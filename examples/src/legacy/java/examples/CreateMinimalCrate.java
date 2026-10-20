package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Legacy variant of {@code CreateMinimalCrate}, written against the
 * released 2.1.2 API (Jackson types in the library). The example code is
 * identical to the current variant; only the canonicalization helper uses
 * the Jackson API instead of the neutral JSON model.
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
   * Same canonicalization as the current variant, but with the legacy
   * Jackson API: entities sorted by their {@code @id}, compact JSON.
   */
  static String canonicalize(String metadata) throws Exception {
    ObjectMapper mapper = MyObjectMapper.getMapper();
    ObjectNode root = (ObjectNode) mapper.readTree(metadata);
    JsonNode graph = root.path("@graph");
    if (graph.isArray()) {
      List<JsonNode> entities = new ArrayList<>();
      graph.forEach(entities::add);
      entities.sort(
        Comparator.comparing(
          entity -> entity.path("@id").asText(""),
          String.CASE_INSENSITIVE_ORDER
        )
      );
      ArrayNode graphArray = (ArrayNode) graph;
      graphArray.removeAll();
      entities.forEach(graphArray::add);
    }
    return mapper.writeValueAsString(root);
  }
}
