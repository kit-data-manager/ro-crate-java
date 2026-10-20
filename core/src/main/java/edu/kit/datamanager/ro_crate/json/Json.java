package edu.kit.datamanager.ro_crate.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
    return uniqueCandidate(
      ServiceLoader.load(JsonAdapter.class).iterator(),
      JsonAdapter.class
    );
  }

  /**
   * Returns the single candidate or throws an exception that names the
   * backend artifacts to add or exclude, so users who do not know the module
   * layout can fix the classpath themselves.
   */
  static <T> T uniqueCandidate(Iterator<T> candidates, Class<T> type) {
    if (!candidates.hasNext()) {
      throw new IllegalStateException(
        "No " +
          type.getSimpleName() +
          " found on the classpath. Add a JSON " +
          "backend as a dependency: edu.kit.datamanager:ro-crate-java " +
          "(Jackson 2 backend) or edu.kit.datamanager:ro-crate-java-jackson3 " +
          "(Jackson 3 backend)."
      );
    }
    T discovered = candidates.next();
    if (candidates.hasNext()) {
      throw new IllegalStateException(
        "Multiple " +
          type.getSimpleName() +
          " implementations found on the " +
          "classpath; ro-crate-java supports only one JSON backend at a " +
          "time. Keep the backend matching your Jackson version and exclude " +
          "the other one, e.g. exclude edu.kit.datamanager:ro-crate-java " +
          "(Jackson 2 backend) and add " +
          "edu.kit.datamanager:ro-crate-java-jackson3 (Jackson 3 backend)."
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
