package edu.kit.datamanager.ro_crate.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Unit tests for the neutral, Jackson-free JSON tree model. */
class NeutralJsonModelTest {

  @Test
  void objectPreservesInsertionOrderAndOverwritesInPlace() {
    JsonObject object = JsonObject.create();
    object.put("b", "second").put("a", "first");
    assertEquals("b", object.names().get(0));
    object.put("b", "changed");
    assertEquals(2, object.size());
    assertEquals("changed", object.get("b").asText());
  }

  @Test
  void accessorsMirrorJacksonSemantics() {
    JsonObject object = JsonObject.create().put("name", "crate");
    assertNull(object.get("absent"));
    assertTrue(object.path("absent").isMissing());
    assertEquals("", object.path("absent").asText());
    assertEquals("DEF", object.path("absent").asText("DEF"));
    assertTrue(object.has("name"));
    assertFalse(object.has("absent"));
    assertEquals("crate", object.get("name").asText());
    assertEquals("", object.asText()); // containers mirror Jackson: ""
  }

  @Test
  void putNullStoresJsonNull() {
    JsonObject object = JsonObject.create().putNull("x");
    assertTrue(object.get("x").isNull());
  }

  @Test
  void removeWorksByIndexAndName() {
    JsonArray array = JsonArray.create().add("a").add("b").add("c");
    assertEquals(new JsonString("b"), array.remove(1));
    assertEquals(2, array.size());

    JsonObject object = JsonObject.create().put("x", 1).put("y", 2);
    assertEquals(new JsonNumber(2), object.remove("y"));
    assertNull(object.get("y"));
  }

  @Test
  void numbersKeepTheirJavaType() throws IOException {
    JsonValue parsed = Json.parse("{\"i\": 1, \"l\": 2147483648, \"d\": 1.5}");
    JsonObject object = (JsonObject) parsed;
    assertTrue(object.get("i").isNumber());
    assertEquals(1, ((JsonNumber) object.get("i")).getValue());
    assertEquals(2147483648L, ((JsonNumber) object.get("l")).getValue());
    assertEquals(1.5, ((JsonNumber) object.get("d")).getValue());
  }

  @Test
  void equalityIsStructuralAndTypeSensitiveForNumbers() {
    JsonObject first = JsonObject.create().put("a", JsonArray.create().add(1));
    JsonObject second = JsonObject.create().put("a", JsonArray.create().add(1));
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());

    // Jackson numeric node equality is class-strict (verified against
    // Jackson 2.22.1: IntNode(1) != LongNode(1), != DoubleNode(1.0))
    assertNotEquals(new JsonNumber(1), new JsonNumber(1.0));
    assertNotEquals(new JsonNumber(1), new JsonNumber(1L));
    assertNotEquals(new JsonNumber(1), new JsonNumber(2147483648L));
    assertNotEquals(new JsonNumber(1), new JsonString("1"));

    // object equality is order-insensitive, mirroring Jackson's LinkedHashMap
    JsonObject ordered = JsonObject.create().put("a", 1).put("b", 2);
    JsonObject reordered = JsonObject.create().put("b", 2).put("a", 1);
    assertEquals(ordered, reordered);
    assertEquals(ordered.hashCode(), reordered.hashCode());
  }

  @Test
  void deepCopyIsIndependent() {
    JsonObject original = JsonObject.create().put(
      "nested",
      JsonObject.create().put("x", 1)
    );
    JsonObject copy = original.deepCopy();
    ((JsonObject) copy.get("nested")).put("x", 2);
    assertEquals(
      1,
      ((JsonNumber) original.path("nested").path("x")).getValue()
    );
    assertEquals(original, original.deepCopy());
  }

  @Test
  void valueNodeSemanticsMirrorJackson() {
    assertTrue(new JsonString("x").isValueNode());
    assertTrue(JsonNull.INSTANCE.isValueNode());
    assertTrue(JsonNull.INSTANCE.isNull());
    assertFalse(JsonNull.INSTANCE.isString());
    assertEquals("null", JsonNull.INSTANCE.asText());
    assertEquals("default", JsonNull.INSTANCE.asText("default"));
    assertEquals("true", JsonBoolean.of(true).asText());
    assertSame(JsonBoolean.TRUE, JsonBoolean.of(true));
    assertTrue(JsonMissing.INSTANCE.isMissing());
  }

  @Test
  void setAllCopiesInOrder() {
    JsonObject source = JsonObject.create().put("a", 1).put("b", 2);
    JsonObject target = JsonObject.create().put("b", 9).put("c", 3);
    target.setAll(source);
    assertEquals(3, target.size());
    assertEquals(new JsonNumber(2), target.get("b"));
    assertEquals(new JsonNumber(1), target.get("a"));
    assertEquals(new JsonNumber(3), target.get("c"));
  }

  @Test
  void fieldsAndIterationExposeValues() {
    JsonObject object = JsonObject.create().put("x", "1").put("y", "2");
    var iterator = object.fields();
    assertEquals("x", iterator.next().getKey());
    assertEquals("y", iterator.next().getKey());
    assertFalse(iterator.hasNext());

    int count = 0;
    for (JsonValue value : object) {
      count++;
      assertTrue(value.isString());
    }
    assertEquals(2, count);
  }

  @Test
  void writeProducesCompactJsonViaAdapter() throws IOException {
    JsonObject object = JsonObject.create()
      .put("name", "crate")
      .put("count", 3)
      .putNull("missing");
    assertEquals(
      "{\"name\":\"crate\",\"count\":3,\"missing\":null}",
      Json.write(object)
    );
  }
}
