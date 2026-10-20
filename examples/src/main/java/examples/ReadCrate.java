package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a crate from a folder and from a zip archive (the archive is created
 * from the fixture with the library's own zip writer) and prints the
 * canonical metadata for both paths.
 */
public class ReadCrate {

  static final Path FIXTURE = Path.of(
      "..", "core", "src", "test", "resources", "crates", "simple_crate");

  public static void main(String[] args) throws Exception {
    Path zip = Path.of(args[0]).getParent().resolve("simple_crate.zip");
    RoCrate folderCrate = Readers.newFolderReader().readCrate(FIXTURE.toString());
    // suppress provenance: it would stamp library version and creation time
    // into the metadata and is not part of the comparison
    Writers.newZipPathWriter()
      .withAutomaticProvenance(null)
      .save(folderCrate, zip.toAbsolutePath().toString());
    RoCrate zipCrate = Readers.newZipPathReader()
      .readCrate(zip.toAbsolutePath().toString());

    StringBuilder report = new StringBuilder();
    report.append("from folder\n")
        .append(canonicalize(folderCrate.getJsonMetadata()))
        .append("\n\nfrom zip\n")
        .append(canonicalize(zipCrate.getJsonMetadata()))
        .append("\n");
    Files.writeString(Path.of(args[0]), report.toString());
  }

  /**
   * Sorts the {@code @graph} entities by their {@code @id} (the entity order
   * follows Java hash iteration and is unspecified) and writes compact JSON,
   * so that runs of different library versions can be compared byte-for-byte.
   */
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
