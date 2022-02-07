package edu.kit.crate.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.serializers.ObjectNodeSerializer;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nikola Tzotchev on 3.2.2022 г.
 * @version 1
 */
public class AbstractEntity {

  /**
   * This set contains the types of an entity (ex. File, Dataset, ect.) It is a set because it does
   * not make sense to have duplicates
   */
  @JsonIgnore
  private Set<String> types;

  /**
   * Contains the whole list of properties of the entity It uses a custom serializer because of
   * cases where a single array element should be displayed as a single value. ex: "key" : ["value"]
   * <=> "key" : "value"
   */
  @JsonUnwrapped
  @JsonSerialize(using = ObjectNodeSerializer.class)
  private ObjectNode properties;

  public ObjectNode getProperties() {
    if (this.types != null) {
      JsonNode node = MyObjectMapper.getMapper().valueToTree(this.types);
      this.properties.set("@type", node);
    }
    return properties;
  }

  public AbstractEntity(AEntityBuilder<?> entityBuilder) {
    this.types = entityBuilder.types;
    this.properties = entityBuilder.properties;
  }

  @JsonIgnore
  public String getId() {
    JsonNode id = this.properties.get("@id");
    return id.asText();
  }

  public void setId(String id) {
    this.properties.put("@id", id);
  }

  public void addProperty(String key, String value) {
    if (key != null && value != null) {
      this.properties.put(key, value);
    }
  }

  public void addProperty(String key, JsonNode value) {
    if (key != null && value != null) {
      this.properties.set(key, value);
    }
  }

  public void addIdProperty(String name, String id) {

    if (name != null && id != null) {
      this.properties.set(name, MyObjectMapper.getMapper().createObjectNode().put("@id", id));
    }
  }

  public void addIdListProperties(String name, List<String> stringList) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ArrayNode node = objectMapper.createArrayNode();
    if (this.properties.get(name) == null) {
      node = objectMapper.createArrayNode();
    } else {
      if (!this.properties.get(name).isArray()) {
        node.add(this.properties.get(name));
      }
    }
    for (String s : stringList) {
      node.add(objectMapper.createObjectNode().put("@id", s));
    }
    if (node.size() == 1) {
      this.properties.set(name, node.get(0));
    } else {
      this.properties.set(name, node);
    }
  }

  public void addType(String type) {
    if (this.types == null) {
      this.types = new HashSet<>();
    }
    this.types.add(type);
    JsonNode node = MyObjectMapper.getMapper().valueToTree(this.types);
    this.properties.set("@type", node);
  }

  public JsonNode getProperty(String propertyKey) {

    return this.properties.get(propertyKey);
  }

  public static abstract class AEntityBuilder<T extends AEntityBuilder<T>> {

    private Set<String> types;
    private ObjectNode properties;

    public AEntityBuilder() {
      this.properties = MyObjectMapper.getMapper().createObjectNode();
    }

    public T setId(String id) {
      this.properties.put("@id", id);
      return self();
    }

    public T addType(String type) {
      if (this.types == null) {
        this.types = new HashSet<>();
      }
      this.types.add(type);
      return self();
    }

    public T addProperty(String key, JsonNode value) {
      this.properties.set(key, value);
      return self();
    }

    public T addProperty(String key, String value) {
      this.properties.put(key, value);
      return self();
    }

    public T addIdProperty(String name, String id) {
      if (name != null && id != null) {
        if (this.properties.get(name) == null) {
          this.properties.set(name, MyObjectMapper.getMapper().createObjectNode().put("@id", id));
        } else {
          JsonNode nodes = this.properties.get(name);
          if (nodes.isArray()) {
            ArrayNode ns = (ArrayNode) nodes;
            ns.add(MyObjectMapper.getMapper().createObjectNode().put("@id", id));
            this.properties.set(name, ns);
          } else {
            ArrayNode newNodes = MyObjectMapper.getMapper().createArrayNode();
            newNodes.add(nodes);
            newNodes.add(MyObjectMapper.getMapper().createObjectNode().put("@id", id));
            this.properties.set(name, newNodes);
          }
        }
      }
      return self();
    }

    public T setAll(ObjectNode properties) {
      this.properties = properties;
      return self();
    }

    public abstract T self();

    abstract public AbstractEntity build();
  }

}
