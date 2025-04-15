package edu.kit.datamanager.ro_crate.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import edu.kit.datamanager.ro_crate.special.IdentifierUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * The class representing the crate json-ld context.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class RoCrateMetadataContext implements CrateMetadataContext {

  public static final String DEFAULT_CONTEXT = "https://w3id.org/ro/crate/1.1/context";
  protected static final String DEFAULT_CONTEXT_LOCATION = "default_context/version1.1.json";
  protected static JsonNode defaultContext = null;

  protected final Set<String> urls = new HashSet<>();
  protected final HashMap<String, String> contextMap = new HashMap<>();
  // we need to keep the ones that are no coming from url
  // for the final representation
  protected final HashMap<String, String> other = new HashMap<>();

  /**
   * Default constructor for the creation of the default context.
   */
  public RoCrateMetadataContext() {
    this.addToContextFromUrl(DEFAULT_CONTEXT);
  }

  /**
   * Constructor for creating the context from a list of url.
   *
   * @param urls the url list with different context.
   */
  public RoCrateMetadataContext(Collection<String> urls) {
    urls.forEach(this::addToContextFromUrl);
  }

  /**
   * Constructor for creating the context from a json object.
   *
   * @param context the Json object of the context.
   */
  public RoCrateMetadataContext(JsonNode context) {

    Consumer<JsonNode> addPairs = x -> {
      var iterate = x.fields();
      while (iterate.hasNext()) {
        var next = iterate.next();
        this.other.put(next.getKey(), next.getValue().asText());
        this.contextMap.put(next.getKey(), next.getValue().asText());
      }
    };
    if (context.isArray()) {
      for (JsonNode jsonNode : context) {
        if (jsonNode.isTextual()) {
          this.addToContextFromUrl(jsonNode.asText());
        } else if (jsonNode.isObject()) {
          addPairs.accept(jsonNode);
        }
      }
    } else if (context.isObject()) {
      addPairs.accept(context);
    } else {
      this.addToContextFromUrl(context.asText());
    }
  }

  @Override
  public ObjectNode getContextJsonEntity() {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ArrayNode array = objectMapper.createArrayNode();
    ObjectNode jsonNode = objectMapper.createObjectNode();
    ObjectNode finalNode = objectMapper.createObjectNode();
    for (String e : urls) {
      array.add(e);
    }
    for (Map.Entry<String, String> s : other.entrySet()) {
      jsonNode.put(s.getKey(), s.getValue());
    }
    if (!jsonNode.isEmpty()) {
      array.add(jsonNode);
    }
    if (array.size() == 1) {
      finalNode.set("@context", array.get(0));
      return finalNode;
    }
    finalNode.set("@context", array);
    return finalNode;
  }

  @Override
  public boolean checkEntity(AbstractEntity entity) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode node = entity.getProperties().deepCopy();
    node.remove("@id");
    node.remove("@type");

    Set<String> types = objectMapper.convertValue(entity.getProperties().get("@type"),
        new TypeReference<>() {
        });
    // check if the items in the array of types are present in the context
    for (String s : types) {
      // special cases:
      if (s.equals("@id")) {
        // @id will refer to the value of the id of the node
        // so we need to extract this value
        s = entity.getId();
      }
      if (s.equals("@json")) {
        // A linked data builtin type, which is fine.
        continue;
      }
      if (IdentifierUtils.isUrl(s)) {
        // full URLs are considered fine
        continue;
      }

      if (this.contextMap.get(s) == null) {
        System.err.println("type " + s + " is missing from the context!");
        return false;
      }
    }

    // check if the fields of the entity are present in the context
    for (var names = node.fieldNames(); names.hasNext();) {
      String s = names.next();
      if (IdentifierUtils.isUrl(s)) {
        // full URLs are considered fine
        continue;
      }
      if (this.contextMap.get(s) == null) {
        System.err.println("attribute name " + s + " is missing from context;");
        return false;
      }
    }
    return true;
  }

  @Override
  public void addToContextFromUrl(String url) {
    this.urls.add(url);

    ObjectMapper objectMapper = MyObjectMapper.getMapper();

    JsonNode jsonNode = null;
    if (url.equals(DEFAULT_CONTEXT)) {
      if (defaultContext != null) {
        jsonNode = defaultContext;
      } else {
        try {
          jsonNode = objectMapper.readTree(
              getClass().getClassLoader().getResource(DEFAULT_CONTEXT_LOCATION));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    if (jsonNode == null) {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(url);
      CloseableHttpResponse response;
      try {
        response = httpclient.execute(httpGet);
        jsonNode = objectMapper.readValue(response.getEntity().getContent(),
            JsonNode.class);
      } catch (IOException e) {
        System.err.println(String.format("Cannot get context from url %s", url));
        return;
      }
      if (url.equals(DEFAULT_CONTEXT)) {
        defaultContext = jsonNode;
      }
    }
    this.contextMap.putAll(objectMapper.convertValue(jsonNode.get("@context"),
        new TypeReference<>() {
        }));
  }

  @Override
  public void addToContext(String key, String value) {
    this.contextMap.put(key, value);
    this.other.put(key, value);
  }

  @Override
  public String getValueOf(String key) {
    return Optional.ofNullable(this.contextMap.get(key))
            .orElseGet(() -> this.other.get(key));
  }

  @Override
  public Set<String> getKeys() {
    List<String> merged = new ArrayList<>();
    merged.addAll(this.contextMap.keySet());
    merged.addAll(this.other.keySet());
    return Set.copyOf(merged);
  }

  @Override
  public Map<String, String> getPairs() {
    Map<String, String> merged = new HashMap<>();
    merged.putAll(this.contextMap);
    merged.putAll(this.other);
    return Map.copyOf(merged);
  }


  @Override
  public void deleteValuePairFromContext(String key) {
    this.contextMap.remove(key);
    this.other.remove(key);
  }

  @Override
  public void deleteUrlFromContext(String url) {
    this.urls.remove(url);
  }

}
