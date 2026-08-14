package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import edu.kit.datamanager.ro_crate.preview.CratePreview;
import edu.kit.datamanager.ro_crate.special.IdentifierUtils;
import edu.kit.datamanager.ro_crate.util.FileSystemUtil;
import edu.kit.datamanager.ro_crate.util.ZipStreamUtil;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 */
public class WriteZipStreamStrategy implements
        GenericWriterStrategy<OutputStream>,
        ElnFormatWriter<OutputStream> {

    private static final Logger logger = LoggerFactory.getLogger(WriteZipStreamStrategy.class);
    public static final String TMP_DIR = "./.tmp/ro-crate-java/writer-zip-stream-strategy/";

    /**
     * Defines if the zip file will directly contain the crate,
     * or if it will contain a subdirectory with the crate.
     */
    protected boolean createRootSubdir = false;

    /**
     * In streams, we do not have a file name yet (or do not know it),
     * so we need to set a default name for the root subdirectory.
     */
    protected String rootSubdirName = "content";

    @Override
    public ElnFormatWriter<OutputStream> usingElnStyle() {
        this.createRootSubdir = true;
        return this;
    }

    /**
     * Sets the name of a root subdirectory in the zip file.
     * Implicitly also enables the creation of a root subdirectory.
     * If used for ELN files, note the subdirectory name should be the same as the zip
     * files name.
     *
     * @param name the name of the subdirectory
     * @return this instance of ReadZipStreamStrategy
     */
    public WriteZipStreamStrategy setSubdirectoryName(String name) {
        this.rootSubdirName = name;
        this.createRootSubdir = true;
        return this;
    }

    @Override
    public void save(Crate crate, OutputStream destination) throws IOException {
        String innerFolderName = "";
        if (this.createRootSubdir) {
            innerFolderName = FileSystemUtil.filterExtensionsFromFileName(
                    this.rootSubdirName,
                    Set.of("ELN", "ZIP"));
            innerFolderName = FileSystemUtil.ensureTrailingSlash(innerFolderName);
        }
        try (ZipOutputStream zipFile = new ZipOutputStream(destination)) {
            saveMetadataJson(crate, zipFile, innerFolderName);
            saveDataEntities(crate, zipFile, innerFolderName);
            savePreview(crate, zipFile, innerFolderName);
        }
    }

    private void saveDataEntities(Crate crate, ZipOutputStream zipStream, String prefix) throws IOException {
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            this.saveToStream(dataEntity, zipStream, prefix);
        }
    }

    private void saveMetadataJson(Crate crate, ZipOutputStream zipStream, String prefix) throws IOException {
        // write the metadata.json file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(prefix + "ro-crate-metadata.json");
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        // we create an JsonNode only to have the file written pretty
        JsonNode node = objectMapper.readTree(crate.getJsonMetadata());
        String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        // write the ro-crate-metadata

        byte[] buff = new byte[4096];
        int readLen;
        zipStream.putNextEntry(zipParameters);
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            while ((readLen = inputStream.read(buff)) != -1) {
                zipStream.write(buff, 0, readLen);
            }
        }
        zipStream.closeEntry();
    }

    private void savePreview(Crate crate, ZipOutputStream zipStream, String prefix) throws IOException {
        Optional<CratePreview> preview = Optional.ofNullable(crate.getPreview());
        if (preview.isEmpty()) {
            return;
        }
        final String ID = UUID.randomUUID().toString();
        File tmpPreviewFolder = Path.of(TMP_DIR)
                .resolve(ID)
                .toFile();
        FileUtils.forceMkdir(tmpPreviewFolder);
        FileUtils.forceDeleteOnExit(tmpPreviewFolder);

        preview.get().generate(crate, tmpPreviewFolder);
        String[] paths = tmpPreviewFolder.list();
        if (paths == null) {
            throw new IOException("No preview files found in temporary folder. Preview generation failed.");
        }
        for (String path : paths) {
            File file = tmpPreviewFolder.toPath().resolve(path).toFile();
            if (file.isDirectory()) {
                ZipStreamUtil.addFolderToZipStream(
                        zipStream,
                        file,
                        prefix + path);
            } else {
                ZipStreamUtil.addFileToZipStream(
                        zipStream,
                        file,
                        prefix + path);
            }
        }
        try {
            FileUtils.forceDelete(tmpPreviewFolder);
        } catch (IOException e) {
            logger.error("Could not delete temporary preview folder: {}", tmpPreviewFolder);
        }
    }

    private void saveToStream(DataEntity entity, ZipOutputStream zipStream, String prefix) throws IOException {
        if (entity == null) {
            return;
        }

        boolean isDirectory = entity.getPath().toFile().isDirectory();
        String id = entity.getId();
        String filename = IdentifierUtils.decode(id).orElse(id);
        String safeName = sanitizeZipEntryName(filename);
        if (safeName.isEmpty()) {
            logger.warn("Skipping entity '{}': decoded name resolves outside the crate root", id);
            return;
        }
        String entryName = prefix + safeName;
        if (isDirectory) {
            ZipStreamUtil.addFolderToZipStream(
                    zipStream,
                    entity.getPath().toFile(),
                    entryName);
        } else {
            ZipStreamUtil.addFileToZipStream(
                    zipStream,
                    entity.getPath().toFile(),
                    entryName);
        }
    }

    /**
     * Normalizes a decoded entry name into a safe relative path, consistent
     * with the containment check in {@link WriteFolderStrategy#saveToFile}.
     * <p>
     * Absolute paths (leading {@code /}) and Windows drive-qualified paths
     * (e.g. {@code C:/}) are rejected. Internal {@code .} and {@code ..}
     * segments are resolved; if the result escapes the virtual crate root
     * the name is rejected.
     *
     * @param name the raw decoded entry name
     * @return a canonical relative entry name, or an empty string if the name
     *         is absolute, drive-qualified, or escapes the crate root
     */
    private static String sanitizeZipEntryName(String name) {
        // zip entries always use forward slashes as separators
        String normalized = name.replace('\\', '/');

        // Reject absolute paths and Windows drive-qualified paths (e.g. C:/)
        if (normalized.startsWith("/")
                || (normalized.length() >= 2 && normalized.charAt(1) == ':'
                        && Character.isLetter(normalized.charAt(0)))) {
            return "";
        }

        // Normalize "." and ".." segments, rejecting paths that escape the root
        String[] segments = normalized.split("/");
        List<String> stack = new ArrayList<>();
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..")) {
                if (stack.isEmpty()) {
                    return "";
                }
                stack.remove(stack.size() - 1);
            } else {
                stack.add(segment);
            }
        }
        return String.join("/", stack);
    }
}
