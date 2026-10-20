package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Canonical form of crate metadata, used to compare runs of different library
 * versions byte-for-byte: the {@code @graph} entities are sorted by their
 * {@code @id} (the entity order follows Java hash iteration and is
 * unspecified) and the JSON is written compactly.
 *
 * <p>Legacy Jackson implementation for the 2.1.2 examples; the current
 * examples ({@code src/main}) use an equivalent neutral-JSON implementation
 * of the same class name.
 */
final class CanonicalJson {

  private CanonicalJson() {}

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
