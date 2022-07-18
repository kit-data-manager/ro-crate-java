package edu.kit.datamanager.ro_crate.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.special.JsonUtilFunctions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The crate payload is the class containing all the entities in the crate.
 */
public class RoCratePayload implements CratePayload {

  private final HashMap<String, DataEntity> dataEntities;
  private final HashMap<String, ContextualEntity> contextualEntities;
  private final HashMap<String, Set<String>> associatedItems;

  /**
   * The default constructor for instantiating a payload.
   */
  public RoCratePayload() {
    this.dataEntities = new HashMap<>();
    this.contextualEntities = new HashMap<>();
    this.associatedItems = new HashMap<>();
  }

  @Override
  public DataEntity getDataEntityById(String id) {
    return this.dataEntities.get(id);
  }

  @Override
  public ContextualEntity getContextualEntityById(String id) {
    return this.contextualEntities.get(id);
  }

  @Override
  public AbstractEntity getEntityById(String id) {
    var context = this.getContextualEntityById(id);
    var data = this.getDataEntityById(id);
    if (context != null) {
      return context;
    }
    return data;
  }

  @Override
  public void addDataEntity(DataEntity dataEntity) {
    this.addToAssociatedItems(dataEntity);
    this.dataEntities.put(dataEntity.getId(), dataEntity);
    dataEntity.addObserver(new EntityObserver(this));
  }

  @Override
  public void addContextualEntity(ContextualEntity contextualEntity) {
    this.addToAssociatedItems(contextualEntity);
    this.contextualEntities.put(contextualEntity.getId(), contextualEntity);
    contextualEntity.addObserver(new EntityObserver(this));
  }

  @Override
  public void addEntity(AbstractEntity entity) {
    if (entity != null) {
      if (entity instanceof DataEntity) {
        this.addDataEntity((DataEntity) entity);
      } else {
        this.addContextualEntity((ContextualEntity) entity);
      }
    }
  }

  @Override
  public void addEntities(Collection<AbstractEntity> entities) {
    if (entities != null) {
      for (var element : entities) {
        this.addEntity(element);
      }
    }
  }

  /**
   * This method is used to add an entity  linked entities' ids to the map of associatedItems.
   * This will be then used to make the removal of entities from the crate faster.
   *
   * @param abstractEntity the abstract entity passed to the method.
   */
  public void addToAssociatedItems(AbstractEntity abstractEntity) {
    var set = abstractEntity.getLinkedTo();
    for (var e : set) {
      this.associatedItems.computeIfAbsent(e, k -> new HashSet<>());
      this.associatedItems.get(e).add(abstractEntity.getId());
    }
  }

  @Override
  public List<AbstractEntity> getAllEntities() {
    List<AbstractEntity> list = new ArrayList<>();
    list.addAll(this.getAllDataEntities());
    list.addAll(this.getAllContextualEntities());
    return list;
  }

  @Override
  public List<DataEntity> getAllDataEntities() {
    List<DataEntity> list = new ArrayList<>();
    for (HashMap.Entry<String, DataEntity> s : this.dataEntities.entrySet()) {
      list.add(s.getValue());
    }
    return list;
  }

  @Override
  public List<ContextualEntity> getAllContextualEntities() {
    List<ContextualEntity> list = new ArrayList<>();
    for (HashMap.Entry<String, ContextualEntity> s : this.contextualEntities.entrySet()) {
      list.add(s.getValue());
    }
    return list;
  }

  @Override
  public ArrayNode getEntitiesMetadata() {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();

    ArrayNode node = objectMapper.createArrayNode();
    for (DataEntity ent : this.getAllDataEntities()) {
      node.add(objectMapper.convertValue(ent, ObjectNode.class));
    }
    for (ContextualEntity ent : this.getAllContextualEntities()) {
      node.add(objectMapper.convertValue(ent, ObjectNode.class));
    }
    return node;
  }

  @Override
  public void removeEntityById(String id) {
    this.dataEntities.remove(id);
    this.contextualEntities.remove(id);
    this.removeAllOccurrencesOf(id);
  }

  private void removeAllOccurrencesOf(String entityId) {
    for (var e : this.getAllEntitiesFromIds(this.associatedItems.get(entityId))) {
      JsonUtilFunctions.removeFieldsWith(entityId, e.getProperties());
    }
  }

  private List<AbstractEntity> getAllEntitiesFromIds(Set<String> set) {
    List<AbstractEntity> list = new ArrayList<>();
    if (set != null) {
      for (var element : set) {
        var entity = this.getEntityById(element);
        if (entity != null) {
          list.add(entity);
        }
      }
    }
    return list;
  }

}
