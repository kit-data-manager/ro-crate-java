package edu.kit.crate.payload;

import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import java.util.Collection;
import java.util.List;

/**
 * Interface for the ROCrate payload.
 * In the payload of the metadata is stored.
 * It provides useful methods for its management.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface CratePayload {
  DataEntity getDataEntityById(String id);

  ContextualEntity getContextualEntityById(String id);

  AbstractEntity getEntityById(String id);

  void addDataEntity(DataEntity dataEntity);

  void addContextualEntity(ContextualEntity contextualEntity);

  void addEntity(AbstractEntity entity);

  void addEntities(Collection<AbstractEntity> entity);

  List<AbstractEntity> getAllEntities();

  List<DataEntity> getAllDataEntities();

  List<ContextualEntity> getAllContextualEntities();

  ArrayNode getEntitiesMetadata();

  void removeEntityById(String id);
}
