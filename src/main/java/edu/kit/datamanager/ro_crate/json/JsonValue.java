package edu.kit.datamanager.ro_crate.json;

import java.io.IOException;
import java.util.Iterator;

/**
 * A JSON value of the neutral, Jackson-free tree model used inside
 * ro-crate-java.
 *
 * <p>The API deliberately mirrors the subset of Jackson's {@code JsonNode}
 * that the library uses (same method names and semantics), so that domain
 * code can be ported mechanically. This is <b>not</b> a general purpose JSON
 * library: only the operations the library needs are supported.
 *
 * <p>Values are mutable ({@link JsonObject} and {@link JsonArray} can be
 * modified in place), preserve insertion order, and implement deep structural
 * equality.
 */
public abstract class JsonValue implements Iterable<JsonValue> {

  /**
   * Returns a deep copy of this value.
   *
   * @return an independent copy; mutations of the copy do not affect this value
   */
  public abstract JsonValue deepCopy();

  /**
   * @return true for string, number, boolean and null values (mirrors Jackson)
   */
  public boolean isValueNode() {
    return false;
  }

  public boolean isObject() {
    return false;
  }

  public boolean isArray() {
    return false;
  }

  public boolean isNull() {
    return false;
  }

  public boolean isString() {
    return false;
  }

  public boolean isNumber() {
    return false;
  }

  public boolean isBoolean() {
    return false;
  }

  /** @return true for the missing value returned by {@link JsonObject#path} */
  public boolean isMissing() {
    return false;
  }

  /**
   * Mirrors Jackson's {@code path(String)} on non-container nodes: always the
   * missing value. Containers override this.
   */
  public JsonValue path(String name) {
    return JsonMissing.INSTANCE;
  }

  /**
   * Text representation, mirroring Jackson's {@code asText()} semantics:
   * value nodes return their string form, containers return the empty string.
   */
  public abstract String asText();

  /**
   * Mirrors Jackson's {@code asText(String defaultValue)}: returns
   * {@code defaultValue} when {@link #asText()} would return null, otherwise
   * the same as {@link #asText()}.
   */
  public String asText(String defaultValue) {
    String text = asText();
    return text == null ? defaultValue : text;
  }

  /**
   * Iterates over the elements of an array, mirroring Jackson. Calling this on
   * any other type throws {@link UnsupportedOperationException}.
   */
  @Override
  public Iterator<JsonValue> iterator() {
    throw new UnsupportedOperationException(
      "Iteration is only supported on arrays"
    );
  }

  /** Compact JSON representation, delegated to the active {@link JsonAdapter}. */
  @Override
  public String toString() {
    try {
      return Json.write(this);
    } catch (IOException exception) {
      throw new java.io.UncheckedIOException(
        "Serialization of the JSON tree failed",
        exception
      );
    }
  }
}
