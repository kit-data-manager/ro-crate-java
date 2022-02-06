package edu.kit.crate.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.utils.Obj;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class ROCrateMetadataContext implements IROCrateMetadataContext {

  private List<String> url;
  private HashMap<String, String> contextMap;
  // we need to keep the ones that are no coming from url
  // for the final representation
  private HashMap<String, String> other;

  public ROCrateMetadataContext(List<String> url) {
    this.url = new ArrayList<>();
    for (String e : url) {
      this.addToContextFromUrl(e);
    }
    this.contextMap = new HashMap<>();
    this.other = new HashMap<>();
  }

  @Override
  public ObjectNode getContextJsonEntity() {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ArrayNode array = objectMapper.createArrayNode();
    ObjectNode jsonNode = objectMapper.createObjectNode();
    ObjectNode finalNode = objectMapper.createObjectNode();
    for (String e : url) {
      array.add(e);
    }
    for (HashMap.Entry<String, String> s : other.entrySet()) {
      jsonNode.put(s.getKey(), s.getValue());
    }
    if (!jsonNode.isEmpty())
    {
      array.add(jsonNode);
    }
    if (array.size() == 1)
    {
      finalNode.set("@context", array.get(0));
      return finalNode;
    }
    finalNode.set("@context", array);
    return finalNode;
  }

  @Override
  public boolean checkEntity(AbstractEntity entity) {
    return true;
  }

  @Override
  public void addToContextFromUrl(String url) {
    this.url.add(url);
    // TODO: complex http to get the mapping from the basic contest
  }

  @Override
  public void addToContext(String key, String value) {
    this.contextMap.put(key, value);
    this.other.put(key, value);
  }
}
