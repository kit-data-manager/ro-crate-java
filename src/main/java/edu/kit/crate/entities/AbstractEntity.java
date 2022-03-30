package edu.kit.crate.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.serializers.ObjectNodeSerializer;
import edu.kit.crate.entities.validation.EntityValidation;
import edu.kit.crate.entities.validation.JsonSchemaValidation;
import edu.kit.crate.objectmapper.MyObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract Entity parent class of every singe item in the json metadata file
 *
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

  private static EntityValidation entityValidation = new EntityValidation(new JsonSchemaValidation());

  public void setProperties(JsonNode obj) {
    // validate whole entity
    if (entityValidation.entityValidation(obj)) {
      this.properties = obj.deepCopy();
    }
  }


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
    if (this.properties.get("@id") == null) {
      if (entityBuilder.id == null) {
        this.properties.put("@id", UUID.randomUUID().toString());
      } else {
        this.properties.put("@id", entityBuilder.id);
      }
    }
  }

  @JsonIgnore
  public String getId() {
    JsonNode id = this.properties.get("@id");
    return id == null ? null : id.asText();
  }

  public void setId(String id) {
    this.properties.put("@id", id);
  }

  public void addProperty(String key, String value) {
    if (key != null && value != null) {
      this.properties.put(key, value);
    }
  }

  public void addProperty(String key, long value) {
    if (key != null) {
      this.properties.put(key, value);
    }
  }

  public void addProperty(String key, double value) {
    if (key != null) {
      this.properties.put(key, value);
    }
  }

  public void addProperty(String key, JsonNode value) {
    addProperty(this.properties, key, value);
  }

  private static void addProperty(ObjectNode whereToAdd, String key, JsonNode value) {
    if (key != null && value != null) {
      if (entityValidation.fieldValidation(value)) {
        whereToAdd.set(key, value);
      }
    }
  }

  /**
   * Add a property that looks like this:
   * "name" : {"@id" : "id"}
   * If the name property already exists add a second @id to it
   *
   * @param name the "key" of the property
   * @param id   the "id" of the property
   */
  public void addIdProperty(String name, String id) {
    JsonNode jsonNode = addToIdProperty(name, id, this.properties.get(name));
    if (jsonNode != null)
      this.properties.set(name, jsonNode);
  }

  private static JsonNode addToIdProperty(String name, String id, JsonNode property) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    if (name != null && id != null) {
      if (property == null) {
        return objectMapper.createObjectNode().put("@id", id);
      } else {
        if (property.isArray()) {
          ArrayNode ns = (ArrayNode) property;
          ns.add(objectMapper.createObjectNode().put("@id", id));
          return ns;
        } else {
          ArrayNode newNodes = objectMapper.createArrayNode();
          newNodes.add(property);
          newNodes.add(objectMapper.createObjectNode().put("@id", id));
          return newNodes;
        }
      }
    }
    return null;
  }

  /**
   * Adds everything from the stringList to the property "name" as id
   *
   * @param name       the key of the property
   * @param stringList List containing all the id as String
   */
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
    private String id;

    public AEntityBuilder() {
      this.properties = MyObjectMapper.getMapper().createObjectNode();
    }
    protected String getId() {
      return this.id;
    }
    public T setId(String id) {
      this.id = id;
      //this.properties.put("@id", id);
      return self();
    }

    public T addType(String type) {
      if (this.types == null) {
        this.types = new HashSet<>();
      }
      this.types.add(type);
      return self();
    }

    public T addTypes(List<String> types) {
      if (this.types == null) {
        this.types = new HashSet<>();
      }
      this.types.addAll(types);
      return self();
    }

    public T addProperty(String key, JsonNode value) {
      AbstractEntity.addProperty(this.properties, key, value);
      return self();
    }

    public T addProperty(String key, String value) {
      this.properties.put(key, value);
      return self();
    }

    public T addProperty(String key, int value) {
      this.properties.put(key, value);
      return self();
    }

    public T addProperty(String key, double value) {
      this.properties.put(key, value);
      return self();
    }

    public T addIdProperty(String name, String id) {
      JsonNode jsonNode = AbstractEntity.addToIdProperty(name, id, this.properties.get(name));
      if (jsonNode != null) {
        this.properties.set(name, jsonNode);
      }
      return self();
    }

    public T setAll(ObjectNode properties) {
      if (AbstractEntity.entityValidation.entityValidation(properties)) {
        this.properties = properties;
      }
      return self();
    }

    public abstract T self();

    abstract public AbstractEntity build();
  }

}
