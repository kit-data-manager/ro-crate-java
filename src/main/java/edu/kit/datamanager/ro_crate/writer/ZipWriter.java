package edu.kit.datamanager.ro_crate.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Implementation of the writing strategy
 * to provide a way of writing crates to a zip archive.
 */
public class ZipWriter implements WriterStrategy {

  @Override
  public void save(Crate crate, String destination) {
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
      inputStream.close();
      if (crate.getPreview() != null) {
        crate.getPreview().saveAllToZip(zipFile);
      }
    } catch (ZipException | JsonProcessingException e) {
      System.out.println("Exception writing ro-crate-metadata.json file to zip");
      e.printStackTrace();
    } catch (IOException e) {
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
