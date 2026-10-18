package edu.kit.datamanager.ro_crate.other;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.special.JsonUtilFunctions;
import io.json.compare.JSONCompare;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class UtilFunctionsTest {

  @Test
  void testUnwrapSimple() throws IOException {
    InputStream fileJson = UtilFunctionsTest.class.getResourceAsStream(
      "/json/unwrap/simple.json"
    );

    JsonValue node = JsonUtilFunctions.unwrapSingleArray(Json.parse(fileJson));
    JsonValue expectedResult = Json.parse(
      UtilFunctionsTest.class.getResourceAsStream(
        "/json/unwrap/simpleResult.json"
      )
    );
    JSONCompare.assertMatches(node.toString(), expectedResult.toString());
  }

  @Test
  void testGettingIdEntriesFromJsonProperty() {
    JsonObject jsonNode = Json.objectNode();
    jsonNode.put("@id", "string1");

    var result = JsonUtilFunctions.getIdPropertiesFromProperty(jsonNode);
    assertEquals(1, result.size());
    assertEquals("string1", result.stream().findFirst().get());

    JsonArray arrayNode = Json.arrayNode();
    arrayNode.add(Json.objectNode().put("@id", "string1"));
    arrayNode.add(Json.objectNode().put("@id", "string2"));

    result = JsonUtilFunctions.getIdPropertiesFromProperty(arrayNode);
    assertEquals(2, result.size());
    assertTrue(result.contains("string1"));
    assertTrue(result.contains("string2"));
  }

  @Test
  void testGettingIdFromObject() {
    JsonObject jsonNode = Json.objectNode();

    jsonNode.put("random", "not id");
    jsonNode.set("other", Json.arrayNode().add("kdfjdlk").add("kdfjalkfj"));

    jsonNode.set("test", Json.objectNode().put("@id", "test1"));

    jsonNode.set("another", Json.objectNode().put("@id", "test2"));

    jsonNode.set(
      "singleArray",
      Json.arrayNode().add(Json.objectNode().put("@id", "test3"))
    );
    jsonNode.set(
      "multipleArrayEl",
      Json.arrayNode()
        .add(Json.objectNode().put("@id", "test4"))
        .add(Json.objectNode().put("@id", "test5"))
    );

    var set = JsonUtilFunctions.getIdPropertiesFromJsonNode(jsonNode);

    assertEquals(5, set.size());

    for (int i = 1; i <= 5; i++) {
      assertTrue(set.contains("test" + i));
    }
  }
}
