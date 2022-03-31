package edu.kit.crate.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.special.JsonUtilFunctions;

import java.util.*;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class ROCratePayload implements IROCratePayload {

  private HashMap<String, DataEntity> dataEntities;
  private HashMap<String, ContextualEntity> contextualEntities;

  private HashMap<String, Set<String>> associatedItems;

  public ROCratePayload() {
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
    dataEntity.setObserver(new EntityObserver(this));
  }

  @Override
  public void addContextualEntity(ContextualEntity contextualEntity) {
    this.addToAssociatedItems(contextualEntity);
    this.contextualEntities.put(contextualEntity.getId(), contextualEntity);
    contextualEntity.setObserver(new EntityObserver(this));
  }

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

  @Override
  public void removeAllOccurrencesOf(String entityId) {
    for (var e : this.getAllEntitiesFromIds(this.associatedItems.get(entityId))) {
      JsonUtilFunctions.removeFieldsWith(entityId, e.getProperties());
    }
  }

  private List<AbstractEntity> getAllEntitiesFromIds(Set<String> set) {
    List<AbstractEntity> list = new ArrayList<>();
    if (set != null) {
      for (var element : set) {
        var entity = this.getEntityById(element);
        if (entity != null)
          list.add(entity);
      }
    }
    return list;
  }

}
