package edu.kit.crate;

import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.preview.IROCratePreview;

import java.util.List;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface IROCrate {
  IROCratePreview getPreview();

  void setMetadataContext(IROCrateMetadataContext metadataContext);

  RootDataEntity getRootDataEntity();

  void setRootDataEntity(RootDataEntity rootDataEntity);

  DataEntity getJsonDescriptor();

  void setJsonDescriptor(DataEntity jsonDescriptor);

  public String getJsonMetadata();

  public DataEntity getDataEntityById(java.lang.String id);

  public List<DataEntity> getAllDataEntities();

  public ContextualEntity getContextualEntityById(java.lang.String id);

  public AbstractEntity getEntityById(java.lang.String id);

  public void addDataEntity(DataEntity entity);

  public void addContextualEntity(ContextualEntity entity);
}
