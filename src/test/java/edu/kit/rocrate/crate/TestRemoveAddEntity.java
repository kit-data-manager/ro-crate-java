package edu.kit.rocrate.crate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.FileEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import org.junit.jupiter.api.Test;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;

import java.io.IOException;
import java.io.InputStream;

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
    JSONCompare.assertEquals(node, expectedJson, CompareMode.JSON_ARRAY_NON_EXTENSIBLE);

    // remove entity and check if equals to the basic crate
    InputStream empty =
        SerializationTest.class.getResourceAsStream("/json/crate/simple.json");
    JsonNode emptyNode = objectMapper.readTree(empty);
    roCrate.deleteEntityById("survey-responses-2019.csv");
    node = objectMapper.readTree(roCrate.getJsonMetadata());
    JSONCompare.assertEquals(node, emptyNode, CompareMode.JSON_ARRAY_NON_EXTENSIBLE);
  }
}
