package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 */
public class ZipStreamWriter implements StreamWriterStrategy {

    private static Logger logger = LoggerFactory.getLogger(ZipStreamWriter.class);

    @Override
    public void save(Crate crate, OutputStream destination) {
        try (ZipOutputStream zipFile = new ZipOutputStream(destination)) {
            saveMetadataJson(crate, zipFile);
            saveDataEntities(crate, zipFile);
        } catch (IOException e) {
            // can not close ZipOutputStream (threw Exception)
            logger.error("Failed to save ro-crate to zip stream.", e);
        }
    }

    private void saveDataEntities(Crate crate, ZipOutputStream zipStream) {
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            try {
                dataEntity.saveToStream(zipStream);
            } catch (IOException e) {
                logger.error("Could not save " + dataEntity.getId() + " to zip stream!", e);
            }
        }
    }

    private void saveMetadataJson(Crate crate, ZipOutputStream zipStream) {
        try {
            // write the metadata.json file
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setFileNameInZip("ro-crate-metadata.json");
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

            if (crate.getPreview() != null) {
                crate.getPreview().saveAllToStream(str, zipStream);
            }
        } catch (IOException e) {
            logger.error("Exception writing ro-crate-metadata.json file to zip.", e);
        }
    }
}
