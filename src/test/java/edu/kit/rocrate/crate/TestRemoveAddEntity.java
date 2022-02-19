package edu.kit.rocrate.crate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.contextual.PlaceEntity;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import org.junit.jupiter.api.Test;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;
import ro.skyah.comparator.JsonComparator;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRemoveAddEntity {
  @Test
  void testAddRemoveEntity() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();
    InputStream inputStream =
        SerializationTest.class.getResourceAsStream("/json/crate/onlyOneFile.json");
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.readTree(roCrate.getJsonMetadata());
    JSONCompare.assertEquals(node, expectedJson, CompareMode.JSON_ARRAY_NON_EXTENSIBLE, CompareMode.JSON_OBJECT_NON_EXTENSIBLE);

    // remove entity and check if equals to the basic crate
    InputStream empty =
        SerializationTest.class.getResourceAsStream("/json/crate/simple.json");
    JsonNode emptyNode = objectMapper.readTree(empty);
    roCrate.deleteEntityById("survey-responses-2019.csv");
    node = objectMapper.readTree(roCrate.getJsonMetadata());
    JSONCompare.assertEquals(node, emptyNode, CompareMode.JSON_ARRAY_NON_EXTENSIBLE, CompareMode.JSON_OBJECT_NON_EXTENSIBLE);
  }

  @Test
  void withTwoFiles() throws IOException {
    PlaceEntity place = new PlaceEntity.PlaceEntityBuilder()
        .setId("http://sws.geonames.org/8152662/")
        .addProperty("name", "Catalina Park")
        .build();

    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice")
        .addProperty("description", "One of hopefully many Contextual Entities")
        .build();

    FileEntity file = new FileEntity.FileEntityBuilder()
        .setId("data1.txt")
        .addProperty("description", "One of hopefully many Data Entities")
        .addIdProperty("author", person.getId())
        .setContentLocation(place.getId())
        .build();

    file.setAuthor(person.getId());

    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addContextualEntity(place)
        .addContextualEntity(person)
        .addDataEntity(file)
        .addDataEntity(
            new FileEntity.FileEntityBuilder().setId("data2.txt").build()
        )
        .build();

    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode jsonROCrate = objectMapper.readTree(roCrate.getJsonMetadata());
    InputStream inputStream =
        SerializationTest.class.getResourceAsStream("/json/crate/twoFiles.json");
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    JSONCompare.assertEquals(jsonROCrate, expectedJson);

    roCrate.deleteEntityById("data1.txt");
    roCrate.deleteEntityById("data2.txt");
    roCrate.deleteEntityById("#alice");
    roCrate.deleteEntityById("http://sws.geonames.org/8152662/");

    ROCrate roCrate2 = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity").build();
    JsonNode emptyNode = objectMapper.readTree(roCrate2.getJsonMetadata());
    JsonNode node = objectMapper.readTree(roCrate.getJsonMetadata());
    JSONCompare.assertEquals(node, emptyNode, new JsonComparator() {
      public boolean compareValues(Object expected, Object actual) {
        return expected.equals(actual);
      }

      public boolean compareFields(String expected, String actual) {
        return expected.equals(actual);
      }
    });
  }
}
