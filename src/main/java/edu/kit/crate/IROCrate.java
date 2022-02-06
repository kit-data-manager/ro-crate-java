package edu.kit.crate;

import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface IROCrate {
  public String getJsonMetadata();
  public DataEntity getDataEntityById(String id);
  public ContextualEntity getContextualEntityById(String id);
  public AbstractEntity getEntityById(String id);
  public void addDataEntity(DataEntity entity);
  public void addContextualEntity(ContextualEntity entity);
}
