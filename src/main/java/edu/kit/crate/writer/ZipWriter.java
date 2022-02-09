package edu.kit.crate.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ZipWriter implements IWriterStrategy {

  @Override
  public void save(IROCrate crate, String destination) {
    File file = new File(destination);
    ZipFile zipFile = new ZipFile(destination);

    try {
      // write the metadata.json file
      ZipParameters zipParameters = new ZipParameters();
      zipParameters.setFileNameInZip("ro-crate-metadata.json");
      ObjectMapper objectMapper = MyObjectMapper.getMapper();
      // we create an JsonNode only to have the file written pretty
      JsonNode node = objectMapper.readTree(crate.getJsonMetadata());
      String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
      InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
      // write the ro-crate-metadata
      zipFile.addStream(inputStream, zipParameters);
    } catch (ZipException | JsonProcessingException e) {
      System.out.println("Exception writing ro-crate-metadata.json file to zip");
      e.printStackTrace();
    }

    // save all the data entities
    for (DataEntity dataEntity : crate.getAllDataEntities()) {
      try {
        dataEntity.saveToZip(zipFile);
      } catch (ZipException e) {
        System.out.println("could not save " + dataEntity.getId() + " to zip file!");
      }
    }
  }
}
