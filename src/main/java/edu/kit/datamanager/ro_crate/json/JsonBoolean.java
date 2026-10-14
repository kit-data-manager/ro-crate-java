package edu.kit.datamanager.ro_crate.json;

/** The JSON boolean value. */
public final class JsonBoolean extends JsonValue {

  public static final JsonBoolean TRUE = new JsonBoolean(true);
  public static final JsonBoolean FALSE = new JsonBoolean(false);

  private final boolean value;

  private JsonBoolean(boolean value) {
    this.value = value;
  }

  public static JsonBoolean of(boolean value) {
    return value ? TRUE : FALSE;
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public boolean isValueNode() {
    return true;
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public String asText() {
    return Boolean.toString(value);
  }

  @Override
  public JsonBoolean deepCopy() {
    return this; // immutable
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonBoolean other && other.value == value;
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(value);
  }
}
