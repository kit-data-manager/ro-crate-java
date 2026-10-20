package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Legacy variant of {@code ReadCrate}, written against the released 2.1.2
 * API. The example code is identical to the current variant; only the
 * canonicalization helper uses the Jackson API.
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
