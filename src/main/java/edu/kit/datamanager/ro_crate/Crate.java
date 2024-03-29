package edu.kit.datamanager.ro_crate;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import edu.kit.datamanager.ro_crate.context.CrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate.preview.CratePreview;
import edu.kit.datamanager.ro_crate.special.CrateVersion;

/**
 * An interface describing an ROCrate.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface Crate {

  /**
   * Read version from the crate descriptor and return it as a class
   * representation.
   * 
   * NOTE: If there is not version in the crate, it does not comply with the
   * specification.
   * 
   * @return the class representation indication the version of this crate, if
   *         available.
   */
  public Optional<CrateVersion> getVersion();

  /**
   * Returns strings indicating the conformance of a crate with other
   * specifications than the RO-Crate version.
   * 
   * If you need the crate version too, refer to {@link #getVersion()}.
   * 
   * This corresponds technically to all conformsTo values, excluding the RO crate
   * version / specification.
   * 
   * @return a collection of the profiles or specifications this crate conforms
   *         to.
   */
  public Collection<String> getProfiles();

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

  List<ContextualEntity> getAllContextualEntities();

  AbstractEntity getEntityById(java.lang.String id);

  void addDataEntity(DataEntity entity, Boolean toHasPart);

  void addContextualEntity(ContextualEntity entity);

  void deleteEntityById(String entityId);

  void setUntrackedFiles(List<File> files);

  void addFromCollection(Collection<AbstractEntity> entities);

  void addItemFromDataCite(String locationUrl);

  public void deleteValuePairFromContext(String key);

  public void deleteUrlFromContext(String url);

  List<File> getUntrackedFiles();
}
