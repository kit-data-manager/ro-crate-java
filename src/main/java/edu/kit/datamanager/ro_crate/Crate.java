package edu.kit.datamanager.ro_crate;

import java.io.File;
import java.util.Collection;
import java.util.List;

import edu.kit.datamanager.ro_crate.context.CrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate.preview.CratePreview;

/**
 * An interface describing an ROCrate.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface Crate {
  CratePreview getPreview();

  void setMetadataContext(CrateMetadataContext metadataContext);

  RootDataEntity getRootDataEntity();

  void setRootDataEntity(RootDataEntity rootDataEntity);

  ContextualEntity getJsonDescriptor();

  void setJsonDescriptor(ContextualEntity jsonDescriptor);

  String getJsonMetadata();

  DataEntity getDataEntityById(java.lang.String id);

  List<DataEntity> getAllDataEntities();

  ContextualEntity getContextualEntityById(java.lang.String id);

  AbstractEntity getEntityById(java.lang.String id);

  void addDataEntity(DataEntity entity, Boolean toHasPart);

  void addContextualEntity(ContextualEntity entity);

  void deleteEntityById(String entityId);

  void setUntrackedFiles(List<File> files);

  void addFromCollection(Collection<AbstractEntity> entities);

  void addItemFromDataCite(String locationUrl);

  List<File> getUntrackedFiles();
}
