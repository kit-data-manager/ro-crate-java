package examples;

import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Canonical form of crate metadata, used to compare runs of different library
 * versions byte-for-byte: the {@code @graph} entities are sorted by their
 * {@code @id} (the entity order follows Java hash iteration and is
 * unspecified) and the JSON is written compactly.
 *
 * <p>This class exists once per source set: this neutral-JSON implementation
 * is used by the current examples ({@code src/main}); the legacy 2.1.2
 * examples ({@code src/legacy}) use an equivalent Jackson implementation of
 * the same class name.
 */
final class CanonicalJson {

  private CanonicalJson() {}

  static String canonicalize(String metadata) throws Exception {
    JsonObject root = (JsonObject) Json.parse(metadata);
    JsonValue graph = root.path("@graph");
    if (graph.isArray()) {
      List<JsonValue> entities = new ArrayList<>();
      graph.forEach(entities::add);
      entities.sort(
        Comparator.comparing(
          entity -> entity.path("@id").asText(""),
          String.CASE_INSENSITIVE_ORDER
        )
      );
      JsonArray graphArray = (JsonArray) graph;
      while (!graphArray.isEmpty()) {
        graphArray.remove(0);
      }
      entities.forEach(graphArray::add);
    }
    return Json.write(root);
  }
}
