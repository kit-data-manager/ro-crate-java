package edu.kit.datamanager.ro_crate.payload;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;


import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;

import java.util.Collection;
import java.util.Set;

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

  void addEntities(Collection<? extends AbstractEntity> entity);

  Set<AbstractEntity> getAllEntities();

  Set<DataEntity> getAllDataEntities();

  Set<ContextualEntity> getAllContextualEntities();

  JsonArray getEntitiesMetadata();

  void removeEntityById(String id);
}
