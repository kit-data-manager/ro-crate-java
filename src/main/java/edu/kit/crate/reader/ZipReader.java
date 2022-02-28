package edu.kit.crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.objectmapper.MyObjectMapper;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ZipReader implements IReaderStrategy {
  private boolean read = false;

  public void readCrate(String location) {
    try {
      File temp = new File("temp");
      if (temp.exists()) {
        FileUtils.cleanDirectory(new File("temp"));
      } else {
        FileUtils.forceDeleteOnExit(new File("temp"));
      }
      new ZipFile(location).extractAll("temp");
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
