package edu.kit.datamanager.ro_crate.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The SPI through which the Jackson-free core talks to a concrete JSON
 * implementation (Jackson 2 or Jackson 3).
 *
 * <p>Implementations must provide an adapter on the classpath and register it
 * via {@code META-INF/services/edu.kit.datamanager.ro_crate.json.JsonAdapter}
 * (see {@link Json}).
 */
public interface JsonAdapter {
  JsonObject objectNode();

  JsonArray arrayNode();

  /** Parses JSON text into the neutral tree model. */
  JsonValue parse(String json) throws IOException;

  JsonValue parse(File file) throws IOException;

  JsonValue parse(InputStream stream) throws IOException;

  /** Serializes a neutral tree to compact JSON text. */
  String write(JsonValue value) throws IOException;

  /** Serializes a neutral tree to pretty-printed JSON text. */
  String writePretty(JsonValue value) throws IOException;
}
