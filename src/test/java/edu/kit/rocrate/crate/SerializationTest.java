package edu.kit.rocrate.crate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.ROCrate;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class SerializationTest {

  @Test
  void simpleROCrateSerialization() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .build();
    InputStream inputStream =
        SerializationTest.class.getResourceAsStream("/json/crate/simple.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.readTree(roCrate.getJsonMetadata());
    assertEquals(node, expectedJson);
  }

  @Test
  void simpleTestWithBonusContextPair() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addValuePairToContext("@test", "ww.test")
        .build();
    InputStream inputStream =
        SerializationTest.class.getResourceAsStream("/json/crate/simple2.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.readTree(roCrate.getJsonMetadata());
    assertEquals(node, expectedJson);
  }
}
