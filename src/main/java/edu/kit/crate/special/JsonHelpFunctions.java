package edu.kit.crate.special;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.objectmapper.MyObjectMapper;

public class JsonHelpFunctions {
  public static JsonNode unwrapSingleArray(JsonNode node) {
    ObjectNode newNode = MyObjectMapper.getMapper().createObjectNode();
    if (node.isObject()) {
      var itr = node.fields();
      while (itr.hasNext()) {
        var nxt = itr.next();
        newNode.set(nxt.getKey(), unwrapSingleArray(nxt.getValue()));
      }
      return newNode;
    } else if (node.isArray()) {
      if (node.size() == 1) {
        return unwrapSingleArray(node.get(0));
      } else {
        ArrayNode arrayNode = MyObjectMapper.getMapper().createArrayNode();
        for (var n : node) {
          arrayNode.add(unwrapSingleArray(n));
        }
        return arrayNode;
      }
    }
    return node;
  }

  public static void removeFieldsWith(String id, JsonNode node) {
    //MyObjectMapper.getMapper().createObjectNode();
    if (node.isObject()) {
      ObjectNode newNode = (ObjectNode) node;
      var itr = newNode.fields();
      while (itr.hasNext()) {
        var nxt = itr.next();
        if (nxt.getValue().isValueNode()) {
          if (nxt.getValue().asText().equals(id)) {
            newNode.remove(nxt.getKey());
          }
        } else {
          removeFieldsWith(id, nxt.getValue());
        }
      }
    } else if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      for (int i = 0; i < arrayNode.size(); i++) {
        var p = arrayNode.get(i);
        if (p.isValueNode()) {
          if (p.asText().equals(id)) {
            arrayNode.remove(i);
          }
        } else {
          removeFieldsWith(id, p);
        }
      }
    }
  }
}
