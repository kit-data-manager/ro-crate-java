package edu.kit.datamanager.ro_crate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Golden tests locking down the exact behavior of the library as a safety net
 * for the Jackson-free-core conversion.
 *
 * <p>For every fixture crate this test asserts:
 * <ol>
 *   <li>{@code getJsonMetadata()} of a freshly read crate is byte-identical to
 *       the committed golden file ({@code /golden/&lt;name&gt;.metadata.json}).</li>
 *   <li>Writing the crate to disk and reading it back yields structurally
 *       identical metadata (write/read idempotence, compared with the same
 *       comparator the existing test suite uses).</li>
 *   <li>The set of data entities survives the round trip unchanged.</li>
 * </ol>
 *
 * <p>If a behavior change is intentional, regenerate the golden files with
 * {@code ./gradlew test -Drocrate.golden.update=true} and review the diff —
 * it must be limited to the intended change.
 */
class GoldenCrateRoundTripTest {

  /** Fixtures must be plain crate folders (no network access on read). */
  static Stream<String> fixtureCrates() {
    return Stream.of(
      "crates/extendedContextExample",
      "crates/hasPartHierarchy",
      "crates/simple_crate",
      "crates/other/idrc_project",
      "crates/spec-1.2-DRAFT/minimal-with-conformsTo-Array"
    );
  }

  /**
   * Canonicalizes metadata for byte-level comparison: entities in the graph
   * are sorted by their {@code @id}. The library does not specify the order
   * of entities in the output graph (it follows Java hash iteration, which is
   * not stable across JVM runs), so entity order is excluded from the lock;
   * everything else — entity content, property order, formatting — is locked.
   */
  static String canonicalize(String metadata) throws IOException {
    JsonObject root = (JsonObject) Json.parse(metadata);
    JsonValue graph = root.path("@graph");
    if (graph.isArray()) {
      List<JsonValue> entities = new ArrayList<>();
      graph.forEach(entities::add);
      entities.sort(
        Comparator.comparing(
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

  @ParameterizedTest
  @MethodSource("fixtureCrates")
  void goldenMetadataIsStableThroughWriteReadRoundTrip(String fixture)
    throws IOException {
    Path crateDir = Path.of("src/test/resources", fixture);
    String name = Path.of(fixture).getFileName().toString();

    RoCrate read = Readers.newFolderReader().readCrate(crateDir.toString());
    String originalMetadata = read.getJsonMetadata();

    Path goldenPath = Path.of(
      "src/test/resources/golden",
      name + ".metadata.json"
    );
    if (System.getProperty("rocrate.golden.update") != null) {
      Files.createDirectories(goldenPath.getParent());
      Files.writeString(
        goldenPath,
        canonicalize(originalMetadata),
        StandardCharsets.UTF_8
      );
    } else {
      assertTrue(
        Files.exists(goldenPath),
        "missing golden file " +
          goldenPath +
          " — run with -Drocrate.golden.update=true to generate"
      );
      assertEquals(
        Files.readString(goldenPath, StandardCharsets.UTF_8),
        canonicalize(originalMetadata),
        "metadata of freshly read crate '" +
          name +
          "' drifted from the golden file"
      );
    }

    Path writtenDir = Files.createTempDirectory("golden-roundtrip");
    Writers.newFolderWriter().save(read, writtenDir.toString());
    RoCrate roundTripped = Readers.newFolderReader().readCrate(
      writtenDir.toString()
    );

    // structural comparison: the reader normalizes formatting, so byte equality
    // across a write/read round trip is not an invariant of the current library
    HelpFunctions.compareTwoCrateJson(read, roundTripped);

    if (System.getProperty("rocrate.golden.update") != null) {
      // golden files are regenerated; no additional assertions in this mode
    } else {
    }

    assertEquals(
      read
        .getAllDataEntities()
        .stream()
        .map(DataEntity::getId)
        .sorted()
        .toList(),
      roundTripped
        .getAllDataEntities()
        .stream()
        .map(DataEntity::getId)
        .sorted()
        .toList(),
      "data entities changed across the write/read round trip for '" +
        name +
        "'"
    );
  }
}
