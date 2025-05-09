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

import edu.kit.datamanager.ro_crate.util.ZipUtil;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 */
public class ZipStreamStrategy implements GenericWriterStrategy<OutputStream> {

    private static final Logger logger = LoggerFactory.getLogger(ZipStreamStrategy.class);

    @Override
    public void save(Crate crate, OutputStream destination) throws IOException {
        try (ZipOutputStream zipFile = new ZipOutputStream(destination)) {
            saveMetadataJson(crate, zipFile);
            saveDataEntities(crate, zipFile);
        }
    }

    private void saveDataEntities(Crate crate, ZipOutputStream zipStream) throws IOException {
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            saveToStream(dataEntity, zipStream);
        }
    }

    private void saveMetadataJson(Crate crate, ZipOutputStream zipStream) throws IOException {
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
    }

    private void saveToStream(DataEntity entity, ZipOutputStream zipStream) throws IOException {
        if (entity == null) {
            return;
        }

        boolean isDirectory = entity.getPath().toFile().isDirectory();
        if (isDirectory) {
            ZipUtil.addFolderToZipStream(
                    zipStream,
                    entity.getPath().toAbsolutePath().toString(),
                    entity.getId());
        } else {
            ZipUtil.addFileToZipStream(
                    zipStream,
                    entity.getPath().toFile(),
                    entity.getId());
        }
    }
}
