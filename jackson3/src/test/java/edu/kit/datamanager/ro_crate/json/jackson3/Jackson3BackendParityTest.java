package edu.kit.datamanager.ro_crate.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parity tests for the Jackson 3 backend: output must be byte-identical to
 * what the Jackson 2 backend produces (and to the committed golden files,
 * which were generated with the original Jackson 2 based library).
 */
class Jackson3BackendParityTest {

  private static final JsonMapper REFERENCE_MAPPER = JsonMapper.builder()
    .enable(
      tools.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
    )
    .enable(
      tools.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS
    )
    .build();

  @Test
  void parseMatchesJackson3Tree() throws IOException {
    String json =
      "{\"name\":\"crate\",\"items\":[\"one\"],\"nested\":{\"a\":1}}";
    assertEquals(
      Jackson3JsonAdapter.toNeutral(REFERENCE_MAPPER.readTree(json)),
      Json.parse(json)
    );
  }

  @Test
  void writeMatchesJackson3Output() throws IOException {
    JsonObject object = JsonObject.create()
      .put("z", "last")
      .put("a", "first")
      .put("n", 1)
      .put("d", 2.5)
      .put("b", true)
      .putNull("nil")
      .set("items", JsonArray.create().add("x").add("y"));
    assertEquals(
      REFERENCE_MAPPER.writeValueAsString(
        Jackson3JsonAdapter.toJackson(object)
      ),
      Json.write(object)
    );
  }

  @Test
  void roundTripThroughAdapterIsStable() throws IOException {
    String json =
      "{\"@context\":\"https://w3id.org/ro/crate/1.1/context\"," +
      "\"@graph\":[{\"@id\":\"./\",\"name\":\"m\\\"in\",\"num\":1.0," +
      "\"unicode\":\"äöü €\"}]}";
    assertEquals(json, Json.write(Json.parse(json)));
  }

  @Test
  void adapterIsDiscoverableViaServiceLoader() {
    assertEquals(Jackson3JsonAdapter.class, Json.adapter().getClass());
  }

  /**
   * The Jackson 3 backend must produce the same crate metadata as the
   * golden files locked in by the core test suite (which were generated
   * with the original Jackson 2 based library).
   */
  @ParameterizedTest
  @MethodSource("fixtureCrates")
  void crateMetadataMatchesGoldenFiles(String fixture) throws IOException {
    Path crateDir = Path.of("../core/src/test/resources", fixture);
    String name = Path.of(fixture).getFileName().toString();

    edu.kit.datamanager.ro_crate.RoCrate read =
      edu.kit.datamanager.ro_crate.reader.Readers.newFolderReader().readCrate(
        crateDir.toString()
      );
    String metadata = read.getJsonMetadata();

    Path goldenPath = Path.of(
      "../core/src/test/resources/golden",
      name + ".metadata.json"
    );
    assertTrue(
      java.nio.file.Files.exists(goldenPath),
      "missing golden file " + goldenPath
    );
    String golden = java.nio.file.Files.readString(goldenPath);
    assertEquals(
      canonicalize(golden),
      canonicalize(metadata),
      "Jackson 3 backend output drifted from the golden file for '" + name + "'"
    );
  }

  /**
   * Canonicalizes metadata for byte-level comparison by sorting the
   * {@code @graph} entities by their {@code @id} (entity order is unspecified
   * in the library). Same semantics as the core golden test's canonicalizer.
   */
  private static String canonicalize(String metadata) throws IOException {
    JsonObject root = (JsonObject) Json.parse(metadata);
    JsonValue graph = root.path("@graph");
    if (graph.isArray()) {
      java.util.List<JsonValue> entities = new java.util.ArrayList<>();
      graph.forEach(entities::add);
      entities.sort(
        java.util.Comparator.comparing(
          node -> node.path("@id").asText(""),
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

  static Stream<String> fixtureCrates() {
    return Stream.of(
      "crates/extendedContextExample",
      "crates/hasPartHierarchy",
      "crates/simple_crate",
      "crates/other/idrc_project",
      "crates/spec-1.2-DRAFT/minimal-with-conformsTo-Array"
    );
  }
}
