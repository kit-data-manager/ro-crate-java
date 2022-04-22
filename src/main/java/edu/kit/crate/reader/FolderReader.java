package edu.kit.crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A class for reading a crate from a folder.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class FolderReader implements ReaderStrategy {

  @Override
  public ObjectNode readMetadataJson(String location) {
    Path metadata = new File(location).toPath().resolve("ro-crate-metadata.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode objectNode = objectMapper.createObjectNode();
    try {
      objectNode = objectMapper.readTree(metadata.toFile()).deepCopy();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return objectNode;
  }

  @Override
  public File readContent(String location) {
    return new File(location);
  }
}
