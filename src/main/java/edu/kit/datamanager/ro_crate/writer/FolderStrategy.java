package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A class for writing a crate to a folder.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderStrategy implements GenericWriterStrategy<String> {

    private static Logger logger = LoggerFactory.getLogger(FolderStrategy.class);

    @Override
    public void save(Crate crate, String destination) {
        File file = new File(destination);
        try {
            FileUtils.forceMkdir(file);
            ObjectMapper objectMapper = MyObjectMapper.getMapper();
            JsonNode node = objectMapper.readTree(crate.getJsonMetadata());
            String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

            File json = new File(destination, "ro-crate-metadata.json");
            FileUtils.copyInputStreamToFile(inputStream, json);
            inputStream.close();            
            // save also the preview files to the crate destination
            if (crate.getPreview() != null) {
                crate.getPreview().saveAllToFolder(file);
            }
            for (var e : crate.getUntrackedFiles()) {
                if (e.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(e, file);
                } else {
                    FileUtils.copyFileToDirectory(e, file);
                }
            }
        } catch (IOException e) {
            logger.error("Error creating destination directory!", e);
        }
        for (DataEntity dataEntity : crate.getAllDataEntities()) {
            try {
                dataEntity.savetoFile(file);
            } catch (IOException e) {
                logger.error("Cannot save " + dataEntity.getId() + " to destination folder!", e);
            }
        }
    }
}
