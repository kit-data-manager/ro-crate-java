package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a small crate programmatically and prints its metadata in
 * canonical form (entities sorted by their {@code @id}, compact JSON).
 *
 * <p>Run with any backend on the classpath — the example itself is
 * backend-agnostic; the ServiceLoader picks up whichever JSON backend
 * (jackson2 or jackson3) is present.
 */
public class CreateMinimalCrate {

  public static void main(String[] args) throws Exception {
    RoCrate crate = new RoCrate.RoCrateBuilder(
      "Example crate",
      "An example crate built with ro-crate-java",
      "2024",
      "https://creativecommons.org/licenses/by/4.0/"
    )
      .addDataEntity(
        new DataEntity.DataEntityBuilder()
          .setId("survey-responses-2019.csv")
          .addType("File")
          .addProperty("name", "Survey responses")
          .addProperty("contentSize", "26452")
          .addProperty("encodingFormat", "text/csv")
          .build()
      )
      .addContextualEntity(
        new ContextualEntity.ContextualEntityBuilder()
          .setId("https://orcid.org/0000-0002-1825-0097")
          .addType("Person")
          .addProperty("name", "Josiah Carberry")
          .build()
      )
      .build();

    Path output = Path.of(args[0]);
    Files.writeString(output, canonicalize(crate.getJsonMetadata()));
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
