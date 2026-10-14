package edu.kit.datamanager.ro_crate.reader;

import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A class for reading a crate from a folder.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ReadFolderStrategy implements GenericReaderStrategy<String> {

  @Override
  public JsonObject readMetadataJson(String location) throws IOException {
    Path metadata = new File(location)
      .toPath()
      .resolve("ro-crate-metadata.json");
    JsonObject objectNode = Json.objectNode();
    objectNode = (JsonObject) Json.parse(metadata.toFile()).deepCopy();
    return objectNode;
  }

  @Override
  public File readContent(String location) {
    return new File(location);
  }
}
