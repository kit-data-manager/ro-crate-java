package edu.kit.datamanager.ro_crate.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The JSON array value: a mutable, order-preserving list of values. Mirrors
 * Jackson's {@code ArrayNode} API for the operations the library uses.
 */
public final class JsonArray extends JsonValue {

  private final List<JsonValue> items = new ArrayList<>();

  /** @return a new empty array (the neutral equivalent of createArrayNode) */
  public static JsonArray create() {
    return new JsonArray();
  }

  // -- construction / mutation ------------------------------------------------

  public JsonArray add(JsonValue value) {
    // mirrors Jackson's ArrayNode.add: a null argument becomes a null node
    items.add(value == null ? JsonNull.INSTANCE : value);
    return this;
  }

  public JsonArray add(String value) {
    return add(value == null ? JsonNull.INSTANCE : new JsonString(value));
  }

  public JsonArray add(long value) {
    return add(new JsonNumber(value));
  }

  public JsonArray add(double value) {
    return add(new JsonNumber(value));
  }

  public JsonArray add(boolean value) {
    return add(JsonBoolean.of(value));
  }

  public JsonArray addAll(JsonArray other) {
    items.addAll(other.items);
    return this;
  }

  /** @return the removed element (mirrors Jackson) */
  public JsonValue remove(int index) {
    return items.remove(index);
  }

  // -- access -----------------------------------------------------------------

  public JsonValue get(int index) {
    if (index < 0 || index >= items.size()) {
      return null; // mirrors Jackson: out-of-bounds yields null
    }
    return items.get(index);
  }

  /** @return the element, or {@link JsonMissing} when out of bounds */
  public JsonValue path(int index) {
    JsonValue value = get(index);
    return value == null ? JsonMissing.INSTANCE : value;
  }

  @Override
  public int size() {
    return items.size();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public Iterator<JsonValue> iterator() {
    return List.copyOf(items).iterator();
  }

  @Override
  public Stream<JsonValue> valueStream() {
    return List.copyOf(items).stream();
  }

  // -- tree operations --------------------------------------------------------

  @Override
  public JsonArray deepCopy() {
    JsonArray copy = new JsonArray();
    for (JsonValue item : items) {
      copy.add(item.deepCopy());
    }
    return copy;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public String asText() {
    return "";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonArray other && items.equals(other.items);
  }

  @Override
  public int hashCode() {
    return items.hashCode();
  }
}
