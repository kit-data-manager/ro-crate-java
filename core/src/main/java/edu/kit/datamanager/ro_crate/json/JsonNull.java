package edu.kit.datamanager.ro_crate.json;

/**
 * The JSON null value, mirroring Jackson's {@code NullNode}:
 * {@link #asText()} returns the string {@code "null"}, while
 * {@link #asText(String)} returns the given default.
 */
public final class JsonNull extends JsonValue {

  public static final JsonNull INSTANCE = new JsonNull();

  private JsonNull() {}

  @Override
  public boolean isValueNode() {
    return true;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public String asText() {
    return "null";
  }

  @Override
  public String asText(String defaultValue) {
    return defaultValue;
  }

  @Override
  public JsonNull deepCopy() {
    return this; // immutable
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonNull;
  }

  @Override
  public int hashCode() {
    return JsonNull.class.hashCode();
  }
}
