package edu.kit.datamanager.ro_crate.entities;

import static org.junit.jupiter.api.Assertions.*;

import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonString;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractEntityTest {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { " ", "\t", "\n" })
  void mergeIdIntoValue_withInvalidId_returnsEmpty(String invalidId) {
    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      invalidId,
      null
    );
    assertTrue(result.isEmpty(), "Should return empty Optional for invalid ID");

    Optional<JsonValue> result2 = AbstractEntity.mergeIdIntoValue(
      invalidId,
      Json.objectNode()
    );
    assertTrue(
      result2.isEmpty(),
      "Should return empty Optional for invalid ID"
    );
  }

  @Test
  void mergeIdIntoValue_withNullCurrentValue_returnsIdObject() {
    String id = "test-id";
    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(id, null);

    // Should return a value for valid ID
    JsonValue node = result.orElseThrow();
    assertTrue(node.isObject(), "Should return an object");
    assertEquals(id, node.get("@id").asText(), "Should contain the ID");
  }

  @Test
  void mergeIdIntoValue_withExistingIdAsString_returnsEmpty() {
    String id = "test-id";
    JsonValue currentValue = new JsonString(id);

    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      id,
      currentValue
    );
    assertTrue(
      result.isEmpty(),
      "Should return empty when ID already exists as string"
    );
  }

  @Test
  void mergeIdIntoValue_withExistingIdObject_returnsEmpty() {
    String id = "test-id";
    JsonObject currentValue = Json.objectNode();
    currentValue.put("@id", id);

    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      id,
      currentValue
    );
    assertTrue(
      result.isEmpty(),
      "Should return empty when ID already exists as object"
    );
  }

  @Test
  void mergeIdIntoValue_withNonArrayValue_createsArray() {
    String id = "new-id";
    String existingId = "existing-id";
    JsonObject existingValue = Json.objectNode();
    existingValue.put("@id", existingId);

    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      id,
      existingValue
    );

    assertTrue(result.isPresent(), "Should return a value");
    JsonValue node = result.get();
    assertTrue(node.isArray(), "Should be converted to array");
    assertEquals(2, node.size(), "Should contain both values");
    assertEquals(
      existingId,
      node.get(0).get("@id").asText(),
      "Should contain existing ID"
    );
    assertEquals(id, node.get(1).get("@id").asText(), "Should contain new ID");
  }

  @Test
  void mergeIdIntoValue_withExistingArray_addsToArray() {
    String id = "new-id";
    String existingId = "existing-id";

    JsonArray currentValue = Json.arrayNode();
    JsonObject existingIdObj = Json.objectNode();
    existingIdObj.put("@id", existingId);
    currentValue.add(existingIdObj);

    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      id,
      currentValue
    );

    assertTrue(result.isPresent(), "Should return a value");
    JsonValue node = result.get();
    assertTrue(node.isArray(), "Should remain an array");
    assertEquals(2, node.size(), "Should contain both values");
    assertEquals(
      existingId,
      node.get(0).get("@id").asText(),
      "Should contain existing ID"
    );
    assertEquals(id, node.get(1).get("@id").asText(), "Should contain new ID");
  }

  @Test
  void mergeIdIntoValue_withExistingArrayContainingId_returnsEmpty() {
    String id = "test-id";
    JsonArray currentValue = Json.arrayNode();
    JsonObject existingIdObj = Json.objectNode();
    existingIdObj.put("@id", id);
    currentValue.add(existingIdObj);

    Optional<JsonValue> result = AbstractEntity.mergeIdIntoValue(
      id,
      currentValue
    );
    assertTrue(
      result.isEmpty(),
      "Should return empty when ID already exists in array"
    );
  }
}
