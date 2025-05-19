package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

import edu.kit.datamanager.ro_crate.preview.CratePreview;
import edu.kit.datamanager.ro_crate.util.ZipUtil;
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

    /**
     * Defines if the zip file will directly contain the crate,
     * or if it will contain a subdirectory with the crate.
     */
    protected boolean createRootSubdir = false;
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
            String dot = Matcher.quoteReplacement(".");
            String end = Matcher.quoteReplacement("$");
            innerFolderName = this.rootSubdirName
                    // remove .zip or .eln from the end of the file name
                    // (?i) removes case sensitivity
                    .replaceFirst("(?i)" + dot + "zip" + end, "")
                    .replaceFirst("(?i)" + dot + "eln" + end, "");
            if (!innerFolderName.endsWith("/")) {
                innerFolderName += "/";
            }
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
        File tmpPreviewFolder = Path.of("./.tmp/ro-crate-java/writer-zipStrategy/")
                .resolve(ID)
                .toFile();
        FileUtils.forceMkdir(tmpPreviewFolder);
        FileUtils.forceDeleteOnExit(tmpPreviewFolder);

        preview.get().generate(crate, tmpPreviewFolder);
        String[] paths = tmpPreviewFolder.list();
        if (paths == null) {
            throw new IOException("No files found in temporary folder");
        }
        for (String path : paths) {
            File file = tmpPreviewFolder.toPath().resolve(path).toFile();
            if (file.isDirectory()) {
                ZipUtil.addFolderToZipStream(
                        zipStream,
                        file,
                        prefix + path);
            } else {
                ZipUtil.addFileToZipStream(
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
        if (isDirectory) {
            ZipUtil.addFolderToZipStream(
                    zipStream,
                    entity.getPath().toAbsolutePath().toString(),
                    prefix + entity.getId());
        } else {
            ZipUtil.addFileToZipStream(
                    zipStream,
                    entity.getPath().toFile(),
                    prefix + entity.getId());
        }
    }
}
