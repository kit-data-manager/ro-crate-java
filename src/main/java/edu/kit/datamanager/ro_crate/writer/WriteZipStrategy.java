package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.preview.CratePreview;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 */
public class WriteZipStrategy implements
        GenericWriterStrategy<String>,
        ElnFormatWriter<String>
{
    private static final Logger logger = LoggerFactory.getLogger(WriteZipStrategy.class);

    /**
     * Defines if the zip file will directly contain the crate,
     * or if it will contain a subdirectory with the crate.
     */
    protected boolean createRootSubdir = false;

    @Override
    public ElnFormatWriter<String> usingElnStyle() {
        this.createRootSubdir = true;
        return this;
    }

    @Override
    public void save(Crate crate, String destination) throws IOException {
        String innerFolderName = "";
        if (this.createRootSubdir) {
            String dot = Matcher.quoteReplacement(".");
            String end = Matcher.quoteReplacement("$");
            innerFolderName = Path.of(destination).getFileName()
                    .toString()
                    // remove .zip or .eln from the end of the file name
                    // (?i) removes case sensitivity
                    .replaceFirst("(?i)" + dot + "zip" + end, "")
                    .replaceFirst("(?i)" + dot + "eln" + end, "");
            if (!innerFolderName.endsWith("/")) {
                innerFolderName += "/";
            }
        }
        try (ZipFile zipFile = new ZipFile(destination)) {
            saveMetadataJson(crate, zipFile, innerFolderName);
            saveDataEntities(crate, zipFile, innerFolderName);
            savePreview(crate, zipFile, innerFolderName);
        }
    }

    private void saveDataEntities(Crate crate, ZipFile zipFile, String prefix) throws IOException {
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            this.saveToZip(dataEntity, zipFile, prefix);
        }
    }

    private void saveMetadataJson(Crate crate, ZipFile zipFile, String prefix) throws IOException {
        // write the metadata.json file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(prefix + "ro-crate-metadata.json");
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        // we create an JsonNode only to have the file written pretty
        JsonNode node = objectMapper.readTree(crate.getJsonMetadata());
        String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            // write the ro-crate-metadata
            zipFile.addStream(inputStream, zipParameters);
        }
    }

    private void savePreview(Crate crate, ZipFile zipFile, String prefix) throws IOException {
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
                ZipParameters parameters = new ZipParameters();
                parameters.setRootFolderNameInZip(prefix + path);
                parameters.setIncludeRootFolder(false);
                zipFile.addFolder(file, parameters);
            } else {
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setFileNameInZip(prefix + path);
                zipFile.addFile(file, zipParameters);
            }
        }
        try {
            FileUtils.forceDelete(tmpPreviewFolder);
        } catch (IOException e) {
            logger.error("Could not delete temporary preview folder: {}", tmpPreviewFolder);
        }
    }

    private void saveToZip(DataEntity entity, ZipFile zipFile, String prefix) throws IOException {
        if (entity == null || entity.getPath() == null) {
            return;
        }

        boolean isDirectory = entity.getPath().toFile().isDirectory();
        if (isDirectory) {
            ZipParameters parameters = new ZipParameters();
            parameters.setRootFolderNameInZip(prefix + entity.getId());
            parameters.setIncludeRootFolder(false);
            zipFile.addFolder(entity.getPath().toFile(), parameters);
        } else {
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setFileNameInZip(prefix + entity.getId());
            zipFile.addFile(entity.getPath().toFile(), zipParameters);
        }
    }
}
