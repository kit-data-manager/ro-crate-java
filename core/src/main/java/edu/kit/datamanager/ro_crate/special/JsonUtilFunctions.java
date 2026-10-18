package edu.kit.datamanager.ro_crate.special;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;



import java.util.HashSet;
import java.util.Set;

/**
 * Class with util functions used with json objects.
 */
public class JsonUtilFunctions {

  /**
   * A function for unwrapping single array elements in a Json object.
   *
   * @param node the json node that should be unwrapped.
   * @return the unwrapped Json.
   */
  public static JsonValue unwrapSingleArray(JsonValue node) {
    JsonObject newNode = Json.objectNode();
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
        JsonArray arrayNode = Json.arrayNode();
        for (var n : node) {
          arrayNode.add(unwrapSingleArray(n));
        }
        return arrayNode;
      }
    }
    return node;
  }

  /**
   * A function for removing id fields from a JSON object.
   *
   * @param id every property with this as id will be removed.
   * @param node the json from which to remove the properties.
   */
  public static void removeFieldsWith(String id, JsonValue node) {
    //Json.objectNode();
    if (node.isObject()) {
      JsonObject newNode = (JsonObject) node;
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
      JsonArray arrayNode = (JsonArray) node;
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

  /**
   * This method extracts from every property of a json objects its id's.
   * The method is intended for flattened json objects.
   *
   * @param node The JsonValue json object.
   * @return the set containing all the strings.
   */
  public static Set<String> getIdPropertiesFromJsonNode(JsonValue node) {
    Set<String> set = new HashSet<>();
    var itr = node.fields();
    while (itr.hasNext()) {
      set.addAll(getIdPropertiesFromProperty(itr.next().getValue()));
    }
    return set;
  }

  /**
   * Extracts the id from a JsonValue property.
   * This method is intended for flattened json-ld
   * that is why it is not recursive
   *
   * @param node the JsonValue property
   * @return Set containing all the id as string
   */
  public static Set<String> getIdPropertiesFromProperty(JsonValue node) {
    Set<String> set = new HashSet<>();
    if (node.isArray()) {
      for (var element : node) {
        if (element.isObject()) {
          var id = element.get("@id");
          if (id != null) {
            set.add(id.asText());
          }
        }
      }
    } else if (node.isObject()) {
      var id = node.get("@id");
      if (id != null) {
        set.add(id.asText());
      }
    }
    return set;
  }

  /**
   * Removes the first instance in the given array that equals the given node.
   * 
   * @param array the array from which to remove the node
   * @param node  the node to remove
   * @return true if an equal node was found and removed, false if no equal node
   *         was found.
   */
  public static boolean removeJsonNodeFromArrayNode(JsonArray array, JsonValue node) {
    for (int i = 0; i < array.size(); i++) {
      if (array.get(i).equals(node)) {
        array.remove(i);
        return true;
      }
    }
    return false;
  }
}
