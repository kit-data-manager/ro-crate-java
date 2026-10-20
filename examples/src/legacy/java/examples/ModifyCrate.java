package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.reader.Readers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Legacy variant of {@code ModifyCrate}, written against the released 2.1.2
 * API. The example code is identical to the current variant; only the
 * canonicalization helper uses the Jackson API.
 */
public class ModifyCrate {

  static final Path FIXTURE = Path.of(
      "..", "core", "src", "test", "resources", "crates", "simple_crate");

  public static void main(String[] args) throws Exception {
    RoCrate crate = Readers.newFolderReader().readCrate(FIXTURE.toString());
    StringBuilder report = new StringBuilder();

    report.append(canonicalize(crate.getJsonMetadata())).append("\n\n");

    // remove the data entity by its id
    crate.deleteEntityById("data.txt");
    report.append("after deleteEntityById(data.txt)\n")
        .append(canonicalize(crate.getJsonMetadata()))
        .append("\n\n");

    // add it back, with different properties
    crate.addDataEntity(
      new DataEntity.DataEntityBuilder()
        .setId("data.txt")
        .addType("File")
        .addProperty("name", "Some data")
        .addProperty("encodingFormat", "text/plain")
        .build());
    report.append("after addDataEntity(data.txt)\n")
        .append(canonicalize(crate.getJsonMetadata()))
        .append("\n\n");

    // modify properties of the root data entity
    crate.getRootDataEntity().removeProperty("datePublished");
    crate.getRootDataEntity().addProperty("keywords", "testing, parity");
    report.append("after root data entity property changes\n")
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
    report.append("after addContextualEntity(organization)\n")
        .append(canonicalize(crate.getJsonMetadata()))
        .append("\n\n");
    crate.deleteEntityById("https://ror.org/04jsmm180");
    report.append("after deleteEntityById(organization)\n")
        .append(canonicalize(crate.getJsonMetadata()));

    Files.writeString(Path.of(args[0]), report.toString());
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
