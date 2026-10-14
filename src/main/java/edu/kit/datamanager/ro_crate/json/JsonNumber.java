package edu.kit.datamanager.ro_crate.json;

/**
 * The JSON number value.
 *
 * <p>The original Java number type is preserved (Integer, Long, Double, ...)
 * so that serialization stays byte-identical to Jackson's output for the same
 * parsed document. Number equality is type-sensitive, mirroring Jackson's
 * node equality: {@code 1} (integer) and {@code 1.0} (double) are not equal.
 */
public final class JsonNumber extends JsonValue {

  private final Number value;

  public JsonNumber(Number value) {
    if (value == null) {
      throw new IllegalArgumentException("JSON numbers cannot be null");
    }
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  @Override
  public boolean isValueNode() {
    return true;
  }

  @Override
  public boolean isNumber() {
    return true;
  }

  @Override
  public String asText() {
    return String.valueOf(value);
  }

  @Override
  public JsonNumber deepCopy() {
    return this; // immutable
  }

  @Override
  public boolean equals(Object obj) {
    // mirrors Jackson's node equality: numbers are only equal when the Java
    // number class matches (IntNode(1) != LongNode(1), != DoubleNode(1.0))
    return (
      obj instanceof JsonNumber other &&
      value.getClass().equals(other.value.getClass()) &&
      value.equals(other.value)
    );
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
