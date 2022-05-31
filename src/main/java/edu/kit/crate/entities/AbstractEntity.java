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
import edu.kit.crate.payload.Observer;
import edu.kit.crate.special.JsonUtilFunctions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Abstract Entity parent class of every singe item in the json metadata file.
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

  private static final EntityValidation entityValidation
      = new EntityValidation(new JsonSchemaValidation());


  @JsonIgnore
  private final Set<String> linkedTo;


  @JsonIgnore
  private final List<Observer> observers;

  public void addObserver(Observer observer) {
    this.observers.add(observer);
  }

  private void notifyObservers() {
    for (var obs : this.observers) {
      obs.update(this.getId());
    }
  }

  /**
   * Constructor that takes a builder and instantiates all the fields from it.
   *
   * @param entityBuilder the entity builder passed to the constructor.
   */
  public AbstractEntity(AbstractEntityBuilder<?> entityBuilder) {
    this.types = entityBuilder.types;
    this.properties = entityBuilder.properties;
    this.linkedTo = entityBuilder.relatedItems;
    this.observers = new ArrayList<>();
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

  /**
   * Returns a Json object containing the properties of the entity.
   *
   * @return ObjectNode representing the properties.
   */
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

  /**
   * Set all the properties from a Json object to the Entity.
   * The entities are first validated to filter any invalid entity properties.
   *
   * @param obj the object that contains all the json properties that should be added.
   */
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

  /**
   * Add a JSON property to the entity.
   * Here the value of the property is a String.
   *
   * @param key   the name of the property (term).
   * @param value the value that the property has.
   */
  public void addProperty(String key, String value) {
    if (key != null && value != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  /**
   * This is another way of adding a property,
   * this time the value is a long integer.
   *
   * @param key   the String key of the property.
   * @param value the long value.
   */
  public void addProperty(String key, long value) {
    if (key != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  /**
   * Another way of adding a property this time the value is a double.
   *
   * @param key   the string key of the property.
   * @param value double value of the property.
   */
  public void addProperty(String key, double value) {
    if (key != null) {
      this.properties.put(key, value);
      this.notifyObservers();
    }
  }

  /**
   * This is the most generic way of adding a property.
   * The value is a JsonNode that could contain anything possible.
   * This is way firstly it is validated that it follows the correct guidelines.
   *
   * @param key   String key of the property.
   * @param value The JsonNode representing the value.
   */
  public void addProperty(String key, JsonNode value) {
    if (addProperty(this.properties, key, value)) {
      notifyObservers();
    }
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
   * If the name property already exists add a second @id to it.
   *
   * @param name the "key" of the property.
   * @param id   the "id" of the property.
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
   * Adds everything from the stringList to the property "name" as id.
   *
   * @param name       the key of the property.
   * @param stringList List containing all the id as String.
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

  /**
   * Adding new type to the property (which may have multiple such ones).
   *
   * @param type the String representing the type.
   */
  public void addType(String type) {
    if (this.types == null) {
      this.types = new HashSet<>();
    }
    this.types.add(type);
    JsonNode node = MyObjectMapper.getMapper().valueToTree(this.types);
    this.properties.set("@type", node);
  }

  /**
   * This a builder inner class that should allow for an easier creating of entities.
   *
   * @param <T> The type of the child builders so that they to can use the methods provided here.
   */
  public abstract static class AbstractEntityBuilder<T extends AbstractEntityBuilder<T>> {

    private Set<String> types;
    protected Set<String> relatedItems;
    private ObjectNode properties;
    private String id;

    public AbstractEntityBuilder() {
      this.properties = MyObjectMapper.getMapper().createObjectNode();
      this.relatedItems = new HashSet<>();
    }

    protected String getId() {
      return this.id;
    }

    /**
     * Setting the id property of the entity.
     *
     * @param id the String representing the id.
     * @return the generic builder.
     */
    public T setId(String id) {
      if (id != null) {
        this.id = id;
      }
      //this.properties.put("@id", id);
      return self();
    }

    /**
     * Adding a type to the builder types.
     *
     * @param type the type to add.
     * @return the generic builder.
     */
    public T addType(String type) {
      if (this.types == null) {
        this.types = new HashSet<>();
      }
      this.types.add(type);
      return self();
    }

    /**
     * Adding multiple types as list.
     *
     * @param types the types in a String List.
     * @return the generic builder.
     */
    public T addTypes(List<String> types) {
      if (this.types == null) {
        this.types = new HashSet<>();
      }
      this.types.addAll(types);
      return self();
    }

    /**
     * Adding a property to the builder.
     *
     * @param key the key of the property in a string.
     * @param value the JsonNode value of te property.
     * @return the generic builder.
     */
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

    public T addProperty(String key, boolean value) {
      this.properties.put(key, value);
      return self();
    }
    /**
     * ID properties are often used when referencing other entities within the ROCrate.
     * This method adds automatically such one.
     * Instead of:
     *  "name": "id"
     * added is :
     *  "name" : {"@id": "id"}
     *
     * @param name the name of the ID property.
     * @param id the ID.
     * @return the generic builder
     */
    public T addIdProperty(String name, String id) {
      JsonNode jsonNode = AbstractEntity.addToIdProperty(name, id, this.properties.get(name));
      if (jsonNode != null) {
        this.properties.set(name, jsonNode);
        this.relatedItems.add(id);
      }
      return self();
    }

    /**
     * This is another way of adding the ID property, this time the whole other Entity is provided.
     *
     * @param name the name of the property.
     * @param entity the other entity that is referenced.
     * @return the generic builder.
     */
    public T addIdProperty(String name, AbstractEntity entity) {
      if (entity != null) {
        return addIdProperty(name, entity.getId());
      }
      return self();
    }

    /**
     * This adds multiple id entities to a single key.
     *
     * @param name the name of the property.
     * @param entities the Collection containing the multiple entities.
     * @return the generic builder.
     */
    public T addIdFromCollectionOfEntities(String name, Collection<AbstractEntity> entities) {
      if (entities != null) {
        for (var e : entities) {
          addIdProperty(name, e);
        }
      }
      return self();
    }

    /**
     * This sets everything from a json object to the property.
     * Can be useful when the entity is already available somewhere.
     *
     * @param properties the Json representing all the properties.
     * @return the generic builder.
     */
    public T setAll(ObjectNode properties) {
      if (AbstractEntity.entityValidation.entityValidation(properties)) {
        this.properties = properties;
        this.relatedItems.addAll(JsonUtilFunctions.getIdPropertiesFromJsonNode(properties));
      }
      return self();
    }

    public abstract T self();

    public abstract AbstractEntity build();
  }

}
