package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.reader.Readers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads a fixture crate from disk and modifies it: adds and removes entities,
 * adds and removes properties. Prints the canonical metadata after each step,
 * so the comparison covers the mutation paths of the library.
 */
public class ModifyCrate {

  static final Path FIXTURE = Path.of(
    "..",
    "core",
    "src",
    "test",
    "resources",
    "crates",
    "simple_crate"
  );

  public static void main(String[] args) throws Exception {
    RoCrate crate = Readers.newFolderReader().readCrate(FIXTURE.toString());
    StringBuilder report = new StringBuilder();

    report.append(canonicalize(crate.getJsonMetadata())).append("\n\n");

    // remove the data entity by its id
    crate.deleteEntityById("data.txt");
    report
      .append("after deleteEntityById(data.txt)\n")
      .append(canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // add it back, with different properties
    crate.addDataEntity(
      new DataEntity.DataEntityBuilder()
        .setId("data.txt")
        .addType("File")
        .addProperty("name", "Some data")
        .addProperty("encodingFormat", "text/plain")
        .build()
    );
    report
      .append("after addDataEntity(data.txt)\n")
      .append(canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // modify properties of the root data entity
    crate.getRootDataEntity().removeProperty("datePublished");
    crate.getRootDataEntity().addProperty("keywords", "testing, parity");
    report
      .append("after root data entity property changes\n")
      .append(canonicalize(crate.getJsonMetadata()))
      .append("\n\n");

    // add and remove a contextual entity
    ContextualEntity organization =
      new ContextualEntity.ContextualEntityBuilder()
        .setId("https://ror.org/04jsmm180")
        .addType("Organization")
        .addProperty("name", "Karlsruhe Institute of Technology")
        .build();
    crate.addContextualEntity(organization);
    report
      .append("after addContextualEntity(organization)\n")
      .append(canonicalize(crate.getJsonMetadata()))
      .append("\n\n");
    crate.deleteEntityById("https://ror.org/04jsmm180");
    report
      .append("after deleteEntityById(organization)\n")
      .append(canonicalize(crate.getJsonMetadata()));

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
