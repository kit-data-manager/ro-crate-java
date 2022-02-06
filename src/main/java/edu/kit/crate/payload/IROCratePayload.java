package edu.kit.crate.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import java.util.Collection;
import java.util.List;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface IROCratePayload {
  public DataEntity getDataEntityById(String id);
  public ContextualEntity getContextualEntityById(String id);
  public AbstractEntity getEntityById(String id);
  public void addDataEntity(DataEntity dataEntity);
  public void addContextualEntity(ContextualEntity contextualEntity);
  public List<AbstractEntity> getAllEntities();
  public List<DataEntity> getAllDataEntities();
  public List<ContextualEntity> getAllContextualEntities();

  public ArrayNode getEntitiesMetadata();
}
