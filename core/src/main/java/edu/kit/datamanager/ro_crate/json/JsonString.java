package edu.kit.datamanager.ro_crate.json;

/** The JSON string value. */
public final class JsonString extends JsonValue {

  private final String value;

  public JsonString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("JSON strings cannot be null");
    }
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean isValueNode() {
    return true;
  }

  @Override
  public boolean isString() {
    return true;
  }

  @Override
  public boolean isTextual() {
    return true;
  }

  /** Mirrors Jackson's {@code TextNode.isEmpty()}. */
  @Override
  public boolean isEmpty() {
    return value.isEmpty();
  }

  @Override
  public String asText() {
    return value;
  }

  @Override
  public JsonString deepCopy() {
    return this; // immutable
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonString other && other.value.equals(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
