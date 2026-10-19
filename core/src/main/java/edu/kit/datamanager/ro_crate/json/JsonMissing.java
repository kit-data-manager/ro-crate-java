package edu.kit.datamanager.ro_crate.json;

/**
 * The result of {@link JsonObject#path(String)} for absent keys, mirroring
 * Jackson's {@code MissingNode}.
 */
public final class JsonMissing extends JsonValue {

  public static final JsonMissing INSTANCE = new JsonMissing();

  private JsonMissing() {}

  @Override
  public boolean isValueNode() {
    return false;
  }

  @Override
  public boolean isMissing() {
    return true;
  }

  @Override
  public String asText() {
    return "";
  }

  /** Mirrors Jackson's {@code MissingNode.asText(default)}: the default. */
  @Override
  public String asText(String defaultValue) {
    return defaultValue;
  }

  @Override
  public JsonMissing deepCopy() {
    return this; // immutable
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonMissing;
  }

  @Override
  public int hashCode() {
    return JsonMissing.class.hashCode();
  }
}
