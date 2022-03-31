package edu.kit.crate;

import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.preview.IROCratePreview;

import java.io.File;
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

  String getJsonMetadata();

  DataEntity getDataEntityById(java.lang.String id);

  List<DataEntity> getAllDataEntities();

  ContextualEntity getContextualEntityById(java.lang.String id);

  AbstractEntity getEntityById(java.lang.String id);

  void addDataEntity(DataEntity entity, Boolean toHasPart);

  void addContextualEntity(ContextualEntity entity);

  void deleteEntityById(String entityId);

  void setUntrackedFiles(List<File> files);

 // void addFromDataCiteSchema(String locationURL);

  List<File> getUntrackedFiles();
}
