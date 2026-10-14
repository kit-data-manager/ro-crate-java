package edu.kit.datamanager.ro_crate.json;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The JSON object value: a mutable, insertion-ordered map from names to
 * values. Mirrors Jackson's {@code ObjectNode} API for the operations the
 * library uses.
 */
public final class JsonObject extends JsonValue {

  private final List<String> names = new ArrayList<>();
  private final List<JsonValue> values = new ArrayList<>();

  /** @return a new empty object (the neutral equivalent of createObjectNode) */
  public static JsonObject create() {
    return new JsonObject();
  }

  // -- construction / mutation ------------------------------------------------

  public JsonObject put(String name, JsonValue value) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    int index = names.indexOf(name);
    if (index >= 0) {
      values.set(index, value);
    } else {
      names.add(name);
      values.add(value);
    }
    return this;
  }

  public JsonObject put(String name, String value) {
    return put(name, value == null ? JsonNull.INSTANCE : new JsonString(value));
  }

  public JsonObject put(String name, int value) {
    return put(name, new JsonNumber(value));
  }

  public JsonObject put(String name, long value) {
    return put(name, new JsonNumber(value));
  }

  public JsonObject put(String name, double value) {
    return put(name, new JsonNumber(value));
  }

  public JsonObject put(String name, float value) {
    return put(name, new JsonNumber(value));
  }

  public JsonObject put(String name, boolean value) {
    return put(name, JsonBoolean.of(value));
  }

  public JsonObject putNull(String name) {
    return put(name, JsonNull.INSTANCE);
  }

  /** Mirrors Jackson's {@code set}: like {@link #put}, but keeps null values. */
  public JsonObject set(String name, JsonValue value) {
    return put(name, value == null ? JsonNull.INSTANCE : value);
  }

  /** Copies all entries of {@code other} into this object, in their order. */
  public JsonObject setAll(JsonObject other) {
    for (var entry : other.entries()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  // -- access -----------------------------------------------------------------

  /** @return the value, or null when absent (mirrors Jackson's {@code get}) */
  public JsonValue get(String name) {
    int index = names.indexOf(name);
    return index < 0 ? null : values.get(index);
  }

  /** @return the value, or {@link JsonMissing} when absent (mirrors {@code path}) */
  public JsonValue path(String name) {
    JsonValue value = get(name);
    return value == null ? JsonMissing.INSTANCE : value;
  }

  public boolean has(String name) {
    return names.contains(name);
  }

  /** @return the removed value, or null when absent (mirrors Jackson) */
  public JsonValue remove(String name) {
    int index = names.indexOf(name);
    if (index < 0) {
      return null;
    }
    names.remove(index);
    return values.remove(index);
  }

  public void remove(Collection<String> namesToRemove) {
    namesToRemove.forEach(this::remove);
  }

  public int size() {
    return names.size();
  }

  public boolean isEmpty() {
    return names.isEmpty();
  }

  /** Property names in insertion order. */
  public List<String> fieldNames() {
    return List.copyOf(names);
  }

  /** Entries in insertion order. */
  public Iterable<Map.Entry<String, JsonValue>> entries() {
    return new ArrayList<>(namedValues());
  }

  /**
   * Mirrors Jackson's {@code fields()}: an iterator over entries in insertion
   * order. The iterator does not support removal.
   */
  public Iterator<Map.Entry<String, JsonValue>> fields() {
    return namedValues().iterator();
  }

  /** Property values in insertion order (mirrors Jackson node iteration). */
  @Override
  public Iterator<JsonValue> iterator() {
    return List.copyOf(values).iterator();
  }

  private List<Map.Entry<String, JsonValue>> namedValues() {
    List<Map.Entry<String, JsonValue>> entries = new ArrayList<>(names.size());
    for (int i = 0; i < names.size(); i++) {
      entries.add(
        new AbstractMap.SimpleImmutableEntry<>(names.get(i), values.get(i))
      );
    }
    return entries;
  }

  // -- tree operations --------------------------------------------------------

  @Override
  public JsonObject deepCopy() {
    JsonObject copy = new JsonObject();
    for (int i = 0; i < names.size(); i++) {
      copy.put(names.get(i), values.get(i).deepCopy());
    }
    return copy;
  }

  @Override
  public boolean isObject() {
    return true;
  }

  @Override
  public String asText() {
    return "";
  }

  @Override
  public boolean equals(Object obj) {
    // mirrors Jackson's ObjectNode.equals, which compares the underlying
    // LinkedHashMap: order-insensitive, entry-wise structural equality
    if (!(obj instanceof JsonObject other) || size() != other.size()) {
      return false;
    }
    for (int i = 0; i < names.size(); i++) {
      if (!values.get(i).equals(other.get(names.get(i)))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    // mirrors Map.hashCode semantics (order-insensitive)
    int hash = 0;
    for (int i = 0; i < names.size(); i++) {
      hash += names.get(i).hashCode() ^ values.get(i).hashCode();
    }
    return hash;
  }
}
