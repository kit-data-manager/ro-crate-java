package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 * Implementation of the reader strategy, providing a way of reading crates from a zip archive.
 */
public class ZipReader implements ReaderStrategy {

  private boolean read = false;

  private void readCrate(String location) {
    try {
      File temp = new File("temp");
      if (temp.exists()) {
        FileUtils.cleanDirectory(new File("temp"));
      }
      new ZipFile(location).extractAll("temp");
      FileUtils.forceDeleteOnExit(new File("temp"));
      this.read = true;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ObjectNode readMetadataJson(String location) {
    if (!read) {
      this.readCrate(location);
    }
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode objectNode;
    File jsonMetadata = new File("temp/ro-crate-metadata.json");
    try {
      objectNode = objectMapper.readTree(jsonMetadata).deepCopy();
      return objectNode;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public File readContent(String location) {
    if (!read) {
      this.readCrate(location);
    }
    return new File("temp");
  }
}
