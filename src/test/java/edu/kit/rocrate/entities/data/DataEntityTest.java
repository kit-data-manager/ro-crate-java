package edu.kit.rocrate.entities.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.DataEntity.DataEntityBuilder;
import java.io.IOException;

import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class DataEntityTest {

  @Test
  void testSerialization() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();

    ObjectNode bigJson = objectMapper.createObjectNode();
    bigJson.put("onefield", "dalkfa");
    ObjectNode address = objectMapper.createObjectNode();
    address.put("street", "x");
    address.put("city", "other");
    address.put("state", "this");
    address.put("zipCode", 66439);
    bigJson.set("nestedObject", address);

    DataEntity file = new DataEntityBuilder()
        .addType("File")
        .setId("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
        .addProperty("name", "RO-Crate specification")
        .addProperty("encodingFormat", "application/pdf")
        .addProperty("url", "https://zenodo.org/record/3541888")
        // this should not be included since it does not follow the json flatten form of the RO-Crate
        .addProperty("test", bigJson)
        .build();

    HelpFunctions.compareEntityWithFile(file, "/json/entities/data/fileEntity.json");
  }

  @Test
  void testSerializationOutside() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode json = objectMapper.createObjectNode();
    json.put("name", "RO-Crate specification");
    json.put("encodingFormat", "application/pdf");
    json.put("url", "https://zenodo.org/record/3541888");
    json.put("@id", "https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf");
    json.put("@type", "File");

    DataEntity data = new DataEntity.DataEntityBuilder().build();
    data.setProperties(json);
    HelpFunctions.compareEntityWithFile(data, "/json/entities/data/fileEntity.json");

    ObjectNode bigJson = objectMapper.createObjectNode();
    bigJson.put("onefield", "dalkfa");
    ObjectNode address = objectMapper.createObjectNode();
    address.put("street", "x");
    address.put("city", "other");
    address.put("state", "this");
    address.put("zipCode", 66439);
    json.set("nestedObject", address);
    // this should not be included into the entity since it does not follow the basic RO-Crate structure
    data.setProperties(json);
    HelpFunctions.compareEntityWithFile(data, "/json/entities/data/fileEntity.json");
  }
}
