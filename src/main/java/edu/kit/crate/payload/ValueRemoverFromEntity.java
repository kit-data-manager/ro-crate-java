package edu.kit.crate.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.objectmapper.MyObjectMapper;

public class ValueRemoverFromEntity {

  public static JsonNode removeFieldsWith(String id, JsonNode node) {
    ObjectNode newNode = MyObjectMapper.getMapper().createObjectNode();
    if (node.isObject()) {
      var itr = node.fields();
      while (itr.hasNext()) {
        var nxt = itr.next();
        if (nxt.getValue().isValueNode()) {
          if (!nxt.getValue().asText().equals(id)) {
            newNode.set(nxt.getKey(),nxt.getValue());
          }
        } else {
          newNode.set(nxt.getKey(), removeFieldsWith(id, nxt.getValue()));
        }
      }
      return newNode;
    } else if (node.isArray()) {
      ArrayNode arrayNode = MyObjectMapper.getMapper().createArrayNode();
      for (JsonNode p : node) {
        if (p.isValueNode()) {
          if (!p.asText().equals(id)) {
            arrayNode.add(p);
          }
        } else {
          arrayNode.add(removeFieldsWith(id, p));
        }
      }
      return arrayNode;
    }
    return newNode;
  }
}
