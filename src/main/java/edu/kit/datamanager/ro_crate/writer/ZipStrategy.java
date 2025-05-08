package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 */
public class ZipStrategy implements GenericWriterStrategy<String> {

    private static final Logger logger = LoggerFactory.getLogger(ZipStrategy.class);

    @Override
    public void save(Crate crate, String destination) throws IOException {
        try (ZipFile zipFile = new ZipFile(destination)) {
            saveMetadataJson(crate, zipFile);
            saveDataEntities(crate, zipFile);
        }
    }

    private void saveDataEntities(Crate crate, ZipFile zipFile) throws ZipException {
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            dataEntity.saveToZip(zipFile);
        }
    }

    private void saveMetadataJson(Crate crate, ZipFile zipFile) throws IOException {
        // write the metadata.json file
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip("ro-crate-metadata.json");
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        // we create an JsonNode only to have the file written pretty
        JsonNode node = objectMapper.readTree(crate.getJsonMetadata());
        String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            // write the ro-crate-metadata
            zipFile.addStream(inputStream, zipParameters);
        }
        if (crate.getPreview() != null) {
            crate.getPreview().saveAllToZip(zipFile);
        }
    }
}
