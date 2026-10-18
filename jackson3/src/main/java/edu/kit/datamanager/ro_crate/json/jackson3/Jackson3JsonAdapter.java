package edu.kit.datamanager.ro_crate.json.jackson3;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.json.JsonAdapter;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonBoolean;
import edu.kit.datamanager.ro_crate.json.JsonMissing;
import edu.kit.datamanager.ro_crate.json.JsonNull;
import edu.kit.datamanager.ro_crate.json.JsonNumber;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonString;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Jackson 3 implementation of the {@link JsonAdapter} SPI.
 *
 * <p>Serialization and parsing are delegated to Jackson 3
 * ({@code tools.jackson}), producing byte-identical output to the Jackson 2
 * backend for the same document. The mapper keeps the parse features the
 * library historically enabled on its shared mapper (see the former
 * {@code MyObjectMapper}).
 *
 * <p>Note on exceptions: Jackson 3 throws unchecked exceptions; the checked
 * {@link IOException} in the SPI is kept for interface compatibility.
 */
public final class Jackson3JsonAdapter implements JsonAdapter {

  private static final JsonMapper MAPPER = JsonMapper.builder()
      .enable(tools.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
      .enable(tools.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
      .build();

  @Override
  public JsonObject objectNode() {
    return JsonObject.create();
  }

  @Override
  public JsonArray arrayNode() {
    return JsonArray.create();
  }

  @Override
  public JsonValue parse(String json) {
    return toNeutral(MAPPER.readTree(json));
  }

  @Override
  public JsonValue parse(File file) throws IOException {
    return toNeutral(MAPPER.readTree(file));
  }

  @Override
  public JsonValue parse(InputStream stream) throws IOException {
    return toNeutral(MAPPER.readTree(stream));
  }

  @Override
  public String write(JsonValue value) {
    return MAPPER.writeValueAsString(toJackson(value));
  }

  @Override
  public String writePretty(JsonValue value) {
    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(toJackson(value));
  }

  /** Converts a Jackson 3 tree into the neutral model. */
  public static JsonValue toNeutral(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return JsonMissing.INSTANCE;
    }
    if (node.isObject()) {
      JsonObject object = JsonObject.create();
      for (var field : node.properties()) {
        object.put(field.getKey(), toNeutral(field.getValue()));
      }
      return object;
    }
    if (node.isArray()) {
      JsonArray array = JsonArray.create();
      for (JsonNode element : node) {
        array.add(toNeutral(element));
      }
      return array;
    }
    if (node.isNull()) {
      return JsonNull.INSTANCE;
    }
    if (node.isString()) {
      return new JsonString(node.stringValue());
    }
    if (node.isBoolean()) {
      return JsonBoolean.of(node.booleanValue());
    }
    if (node.isNumber()) {
      return new JsonNumber(numberOf(node));
    }
    throw new IllegalArgumentException(
        "Unsupported Jackson node type: " + node.getNodeType());
  }

  private static Number numberOf(JsonNode node) {
    if (node.isInt()) {
      return node.intValue();
    }
    if (node.isLong()) {
      return node.longValue();
    }
    if (node.isShort()) {
      return node.shortValue();
    }
    if (node.isBigInteger()) {
      return node.bigIntegerValue();
    }
    if (node.isDouble()) {
      return node.doubleValue();
    }
    if (node.isFloat()) {
      return node.floatValue();
    }
    if (node.isBigDecimal()) {
      return node.decimalValue();
    }
    return node.doubleValue();
  }

  /** Converts the neutral model into a Jackson 3 tree. */
  public static JsonNode toJackson(JsonValue value) {
    if (value instanceof JsonObject object) {
      ObjectNode node = MAPPER.createObjectNode();
      for (var entry : object.entries()) {
        node.set(entry.getKey(), toJackson(entry.getValue()));
      }
      return node;
    }
    if (value instanceof JsonArray array) {
      ArrayNode node = MAPPER.createArrayNode();
      for (JsonValue element : array) {
        node.add(toJackson(element));
      }
      return node;
    }
    if (value instanceof JsonString string) {
      return MAPPER.getNodeFactory().textNode(string.getValue());
    }
    if (value instanceof JsonBoolean bool) {
      return bool.getValue() ? MAPPER.getNodeFactory().booleanNode(true)
          : MAPPER.getNodeFactory().booleanNode(false);
    }
    if (value instanceof JsonNull) {
      return MAPPER.getNodeFactory().nullNode();
    }
    if (value instanceof JsonMissing) {
      return MAPPER.getNodeFactory().missingNode();
    }
    if (value instanceof JsonNumber number) {
      return numberNode(number.getValue());
    }
    throw new IllegalArgumentException(
        "Unsupported neutral JSON value: " + value.getClass());
  }

  private static JsonNode numberNode(Number number) {
    if (number instanceof Integer n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof Long n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof Short n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof Byte n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof Double n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof Float n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof BigInteger n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    if (number instanceof BigDecimal n) {
      return MAPPER.getNodeFactory().numberNode(n);
    }
    // fallback for exotic Number implementations
    return MAPPER.getNodeFactory().numberNode(number.doubleValue());
  }
}
