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
import edu.kit.crate.payload.IObserver;
import edu.kit.crate.special.JsonUtilFunctions;

import java.util.*;

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

  private final static EntityValidation entityValidation = new EntityValidation(new JsonSchemaValidation());


  @JsonIgnore
  private final Set<String> linkedTo;


  @JsonIgnore
  private IObserver observer;

  public void setObserver(IObserver observer) {
    this.observer = observer;
  }

  private void notifyObservers() {
    if (this.observer != null)
      this.observer.update(this.getId());
  }


  public AbstractEntity(AEntityBuilder<?> entityBuilder) {
    this.types = entityBuilder.types;
    this.properties = entityBuilder.properties;
    this.linkedTo = entityBuilder.relatedItems;
    if (this.properties.get("@id") == null) {
      if (entityBuilder.id == null) {
        this.properties.put("@id", UUID.randomUUID().toString());
      } else {
        this.properties.put("@id", entityBuilder.id);
      }
    }
  }

  public Set<String> getLinkedTo() {
    return linkedTo;
  }


  public ObjectNode getProperties() {
    if (this.types != null) {
      JsonNode node = MyObjectMapper.getMapper().valueToTree(this.types);
      this.properties.set("@type", node);
    }
    return properties;
  }

  public JsonNode getProperty(String propertyKey) {
    return this.properties.get(propertyKey);
  }

  @JsonIgnore
  public String getId() {
    JsonNode id = this.properties.get("@id");
    return id == null ? null : id.asText();
  }

  public void setProperties(JsonNode obj) {
    // validate whole entity
    if (entityValidation.entityValidation(obj)) {
      this.properties = obj.deepCopy();
      this.notifyObservers();
    }
  }

  protected void setId(String id) {
    this.properties.put("@id", id);
  }

  public void addProperty(String key, String value) {
    if (key != null && value != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  public void addProperty(String key, long value) {
    if (key != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  public void addProperty(String key, double value) {
    if (key != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  public void addProperty(String key, JsonNode value) {
    if (addProperty(this.properties, key, value))
      notifyObservers();
  }

  private static boolean addProperty(ObjectNode whereToAdd, String key, JsonNode value) {
    if (key != null && value != null) {
      if (entityValidation.fieldValidation(value)) {
        whereToAdd.set(key, value);
        return true;
      }
    }
    return false;
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
    if (jsonNode != null) {
      this.linkedTo.add(id);
      this.properties.set(name, jsonNode);
      this.notifyObservers();
    }
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
      this.linkedTo.add(s);
      node.add(objectMapper.createObjectNode().put("@id", s));
    }
    if (node.size() == 1) {
      this.properties.set(name, node.get(0));
    } else {
      this.properties.set(name, node);
    }
    notifyObservers();
  }

  public void addType(String type) {
    if (this.types == null) {
      this.types = new HashSet<>();
    }
    this.types.add(type);
    JsonNode node = MyObjectMapper.getMapper().valueToTree(this.types);
    this.properties.set("@type", node);
  }


  public static abstract class AEntityBuilder<T extends AEntityBuilder<T>> {

    private Set<String> types;
    protected Set<String> relatedItems;
    private ObjectNode properties;
    private String id;

    public AEntityBuilder() {
      this.properties = MyObjectMapper.getMapper().createObjectNode();
      this.relatedItems = new HashSet<>();
    }

    protected String getId() {
      return this.id;
    }

    public T setId(String id) {
      if (id != null)
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
      if (AbstractEntity.addProperty(this.properties, key, value)) {
        this.relatedItems.addAll(JsonUtilFunctions.getIdPropertiesFromProperty(value));
      }
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
        this.relatedItems.add(id);
      }
      return self();
    }

    public T addIdProperty(String name, AbstractEntity entity) {
      if (entity != null) {
        return addIdProperty(name, entity.getId());
      }
      return self();
    }

    public T addIdFromCollectionOFEntities(String name, Collection<AbstractEntity> entities) {
      if (entities != null) {
        for (var e : entities) {
          addIdProperty(name, e);
        }
      }
      return self();
    }

    public T setAll(ObjectNode properties) {
      if (AbstractEntity.entityValidation.entityValidation(properties)) {
        this.properties = properties;
        this.relatedItems.addAll(JsonUtilFunctions.getIdPropertiesFromJsonNode(properties));
      }
      return self();
    }

    public abstract T self();

    abstract public AbstractEntity build();
  }

}
