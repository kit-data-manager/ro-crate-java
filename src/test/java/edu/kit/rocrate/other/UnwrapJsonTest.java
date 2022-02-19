package edu.kit.rocrate.other;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.special.JsonHelpFunctions;
import org.junit.jupiter.api.Test;
import ro.skyah.comparator.JSONCompare;

import java.io.IOException;
import java.io.InputStream;

public class UnwrapJsonTest {

  @Test
  void testUnwrapSimple() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    InputStream fileJson =
        UnwrapJsonTest.class.getResourceAsStream("/json/unwrap/simple.json");

    JsonNode node = JsonHelpFunctions.unwrapSingleArray(objectMapper.readTree(fileJson));
    JsonNode expectedResult = objectMapper.readTree(
        UnwrapJsonTest.class.getResourceAsStream("/json/unwrap/simpleResult.json")
    );
    JSONCompare.assertEquals(node, expectedResult);
  }
}
