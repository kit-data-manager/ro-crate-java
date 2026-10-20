package edu.kit.datamanager.ro_crate.json;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The discovery failures must tell a user who does not know the module layout
 * exactly what to add or exclude, so the messages must name the backend
 * artifacts.
 */
class JsonDiscoveryTest {

  private static JsonAdapter stubAdapter() {
    return new JsonAdapter() {
      @Override
      public JsonObject objectNode() {
        return null;
      }

      @Override
      public JsonArray arrayNode() {
        return null;
      }

      @Override
      public JsonValue parse(String json) {
        return null;
      }

      @Override
      public JsonValue parse(File file) {
        return null;
      }

      @Override
      public JsonValue parse(InputStream stream) {
        return null;
      }

      @Override
      public String write(JsonValue value) {
        return null;
      }

      @Override
      public String writePretty(JsonValue value) {
        return null;
      }
    };
  }

  @Test
  void missingBackendMessageNamesTheBackendArtifacts() {
    var exception = assertThrows(
      IllegalStateException.class,
      () -> Json.uniqueCandidate(
        Collections.<JsonAdapter>emptyIterator(),
        JsonAdapter.class
      )
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java"),
      "message should name the Jackson 2 artifact: " + exception.getMessage()
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java-jackson3"),
      "message should name the Jackson 3 artifact: " + exception.getMessage()
    );
  }

  @Test
  void multipleBackendsMessageSuggestsTheFix() {
    JsonAdapter adapter = stubAdapter();
    var exception = assertThrows(
      IllegalStateException.class,
      () -> Json.uniqueCandidate(
        List.<JsonAdapter>of(adapter, adapter).iterator(),
        JsonAdapter.class
      )
    );
    assertTrue(
      exception.getMessage().contains("exclude"),
      "message should suggest excluding one backend: " + exception.getMessage()
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java-jackson3"),
      "message should name the Jackson 3 artifact: " + exception.getMessage()
    );
  }
}
