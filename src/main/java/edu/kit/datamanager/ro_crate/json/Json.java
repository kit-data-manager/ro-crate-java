package edu.kit.datamanager.ro_crate.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

/**
 * Static access point to the JSON operations of the neutral tree model.
 *
 * <p>The concrete {@link JsonAdapter} (Jackson 2 or Jackson 3) is discovered
 * via {@link ServiceLoader}; exactly one adapter must be on the classpath.
 * Shading users or special setups can override discovery with
 * {@link #setAdapter}.
 */
public final class Json {

  private static volatile JsonAdapter adapter;

  private Json() {}

  public static JsonAdapter adapter() {
    JsonAdapter result = adapter;
    if (result == null) {
      synchronized (Json.class) {
        result = adapter;
        if (result == null) {
          adapter = result = discoverAdapter();
        }
      }
    }
    return result;
  }

  /** Overrides adapter discovery; intended for tests and shaded builds. */
  public static void setAdapter(JsonAdapter replacement) {
    adapter = replacement;
  }

  private static JsonAdapter discoverAdapter() {
    var candidates = ServiceLoader.load(JsonAdapter.class).iterator();
    if (!candidates.hasNext()) {
      throw new IllegalStateException(
        "No JsonAdapter found on the classpath. Add the ro-crate-java " +
          "jackson2 or jackson3 module as a dependency."
      );
    }
    JsonAdapter discovered = candidates.next();
    if (candidates.hasNext()) {
      throw new IllegalStateException(
        "Multiple JsonAdapters found on the classpath; ro-crate-java " +
          "supports only one JSON backend at a time."
      );
    }
    return discovered;
  }

  public static JsonObject objectNode() {
    return adapter().objectNode();
  }

  public static JsonArray arrayNode() {
    return adapter().arrayNode();
  }

  public static JsonValue parse(String json) throws IOException {
    return adapter().parse(json);
  }

  public static JsonValue parse(File file) throws IOException {
    return adapter().parse(file);
  }

  public static JsonValue parse(InputStream stream) throws IOException {
    return adapter().parse(stream);
  }

  public static String write(JsonValue value) throws IOException {
    return adapter().write(value);
  }

  public static String writePretty(JsonValue value) throws IOException {
    return adapter().writePretty(value);
  }
}
