package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity.DataEntityBuilder;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    assertNull(file.getProperty("test"));
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

    ObjectNode address = objectMapper.createObjectNode();
    address.put("street", "x");
    address.put("city", "other");
    address.put("state", "this");
    address.put("zipCode", 66439);
    json.set("nestedObject", address);
    // this should not be included into the entity since it does not follow the basic RO-Crate structure
    data.setProperties(json);
    assertNotEquals("", data.getId());
    HelpFunctions.compareEntityWithFile(data, "/json/entities/data/fileEntity.json");
  }

  @Test
  void testUrlCheck() {
    // get the std output redirected, so we can see if there is something written
    PrintStream standardOut = System.out;
    ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));

    // create data entity with random ID that is not a remote URI
    // this should create a warning in the console
    new DataEntity.DataEntityBuilder()
        .addType("File")
        .setId("dfkjdfkj")
        .build();

    assertEquals("This Data Entity remote ID does not resolve to a valid URL.", outputStreamCaptor.toString().trim());
    // also clear the stream so that we get it ready for the next comparison
    outputStreamCaptor.reset();

    // this entity id is a valid URL so there should not be any console information
    new DataEntity.DataEntityBuilder()
        .addType("File")
        .setId("https://www.example.com/19")
        .build();


    assertEquals("", outputStreamCaptor.toString().trim());
    URL url =
        HelpFunctions.class.getResource("/json/crate/simple2.json");
    assert url != null;

    // this data entity is not a remote one, so there should not be any messages
    new DataEntity.DataEntityBuilder()
        .addType("File")
        .setSource(new File(url.getFile()))
        .build();

    outputStreamCaptor.reset();
    assertEquals("", outputStreamCaptor.toString().trim());
    System.setOut(standardOut);
  }
  @Test
  void testEncodedUrl() {
    // get the std output redirected, so we can see if there is something written
    PrintStream standardOut = System.out;
    ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));

    // this entity id is a valid URL so there should not be any console information
    new DataEntity.DataEntityBuilder()
            .addType("File")
            .setId("https%3A%2F%2Fgithub.com%2Fkit-data-manager%2Fro-crate-java%2Fissues%2F5")
            .build();


    assertEquals("", outputStreamCaptor.toString().trim());
    URL url =
            HelpFunctions.class.getResource("/json/crate/simple2.json");
    assert url != null;

    // this data entity is not a remote one, so there should not be any messages
    new DataEntity.DataEntityBuilder()
            .addType("File")
            .setSource(new File(url.getFile()))
            .build();

    assertEquals("", outputStreamCaptor.toString().trim());

    System.setOut(standardOut);
  }
}
