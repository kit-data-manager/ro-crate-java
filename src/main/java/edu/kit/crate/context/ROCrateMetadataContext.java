package edu.kit.crate.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class ROCrateMetadataContext implements IROCrateMetadataContext {

  private final static String DEFAULT_CONTEXT = "https://w3id.org/ro/crate/1.1/context";
  private static JsonNode defaultContext = null;

  private final List<String> url;
  private final HashMap<String, String> contextMap;
  // we need to keep the ones that are no coming from url
  // for the final representation
  private final HashMap<String, String> other;

  public ROCrateMetadataContext() {
    this.url = new ArrayList<>();
    this.contextMap = new HashMap<>();
    this.other = new HashMap<>();
    this.addToContextFromUrl(DEFAULT_CONTEXT);
  }

  public ROCrateMetadataContext(List<String> url) {
    this.url = new ArrayList<>();
    this.contextMap = new HashMap<>();
    this.other = new HashMap<>();
    for (String e : url) {
      this.addToContextFromUrl(e);
    }
  }

  public ROCrateMetadataContext(JsonNode context) {
    this.url = new ArrayList<>();
    this.other = new HashMap<>();
    this.contextMap = new HashMap<>();

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
    for (String e : url) {
      array.add(e);
    }
    for (HashMap.Entry<String, String> s : other.entrySet()) {
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

  public ObjectNode getFromSchema(String type) {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    String url = this.contextMap.get(type);
    HttpGet httpGet = new HttpGet(url);
    CloseableHttpResponse response;
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      response = httpclient.execute(httpGet);
      Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
      Element el = doc.selectFirst("script[type]");
      assert el != null;
      return objectMapper.readValue(el.data(), ObjectNode.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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
    for (var s : types) {
      if (this.contextMap.get(s) == null) {
        System.err.println("type " + s + " is missing from the context!");
        return false;
      }
    }

    // check if the fields of the entity are present in the context
    for (var names = node.fieldNames(); names.hasNext(); ) {
      String s = names.next();
      if (this.contextMap.get(s) == null) {
        System.err.println("entity " + s + " is missing from context;");
        return false;
      }
    }
    return true;
  }

  /*
  @Override
  public boolean checkEntity(AbstractEntity entity) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();

    // expand our node
    ObjectNode node = entity.getProperties().deepCopy();
    node.remove("@id");
    node.remove("@type");
    node.put("@context", "https://schema.org/");

    ArrayNode expandedNode = null;
    try {
      com.apicatalog.jsonld.document.Document document = JsonDocument
          .of(new ByteArrayInputStream(node.toString().getBytes(StandardCharsets.UTF_8)));

      expandedNode = (ArrayNode) objectMapper.readTree(JsonLd.expand(document).get().toString());
    } catch (JsonLdError | JsonProcessingException e) {
      e.printStackTrace();
    }

    // get the context
    Set<String> types = objectMapper.convertValue(entity.getProperties().get("@type"),
        new TypeReference<>() {
        });

    ArrayNode comp = objectMapper.createArrayNode();
    // get the schema for every one of the types
    for (String type : types) {

      JsonNode jsonArr = this.getFromSchema(type).deepCopy();
      jsonArr = jsonArr.get("@graph");

      for (var elements = jsonArr.elements(); elements.hasNext(); ) {

        var el = elements.next();
        ObjectNode filtered = objectMapper.createObjectNode();
        for (var fields = el.fields(); fields.hasNext(); ) {

          var value = fields.next();
          if (Pattern.matches("@id", value.getKey())) {
            filtered.set(value.getKey(), value.getValue());
          }

        }
        comp.add(filtered);
      }
    }

    ObjectNode contextNode = objectMapper.createObjectNode();
    contextNode.put("@context", "https://schema.org/");
    contextNode.set("array", comp);
    Set<String> ent = new HashSet<>();

    try {
      com.apicatalog.jsonld.document.Document document = JsonDocument
          .of(new ByteArrayInputStream(contextNode.toString().getBytes(StandardCharsets.UTF_8)));
      JsonNode expandedContextNode = objectMapper.readTree(
          JsonLd.expand(document).get().toString());

      for (var e : expandedContextNode.findValues("@id")) {
        ent.add(e.asText());
      }
    } catch (JsonLdError | JsonProcessingException e) {
      e.printStackTrace();
    }

    assert expandedNode != null;
    if (expandedNode.isEmpty()) {
      return true;
    }
    var json = expandedNode.get(0);
    for (var names = json.fieldNames(); names.hasNext(); ) {
      String s = names.next();
      if (!ent.contains(s) && this.other.get(s) == null) {
        System.err.println("entity " + s + " is missing from context;");
      }
    }
    return true;
  }
*/
  @Override
  public void addToContextFromUrl(String url) {
    this.url.add(url);

    ObjectMapper objectMapper = MyObjectMapper.getMapper();

    JsonNode jsonNode;
    if (url.equals(DEFAULT_CONTEXT) && defaultContext != null) {
      jsonNode = defaultContext;
    } else {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(url);
      CloseableHttpResponse response;
      try {
        response = httpclient.execute(httpGet);
        jsonNode = objectMapper.readValue(response.getEntity().getContent(),
            JsonNode.class);
      } catch (IOException e) {
        System.err.println("Cannot get context from this url.");
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
}
