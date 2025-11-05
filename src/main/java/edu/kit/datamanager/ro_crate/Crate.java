package edu.kit.datamanager.ro_crate;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.kit.datamanager.ro_crate.context.CrateMetadataContext;
import edu.kit.datamanager.ro_crate.crate.HierarchyRecognitionConfig;
import edu.kit.datamanager.ro_crate.crate.HierarchyRecognitionResult;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
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
   * Mark the crate as imported, i.e. it has been read from a file
   * or is for other reasons not considered a new crate.
   * <p>
   * This is useful mostly for readers to indicate this in case
   * the crate may not have any provenance information yet and
   * should still be recognized as an imported crate.
   *
   * @return this crate, for convenience.
   */
  Crate markAsImported();

  /**
   * Check if the crate is marked as imported.
   * <p>
   * If true, it indicates that the crate has been read from a file
   * or is for other reasons not considered a new crate.
   *
   * @return true if the crate is marked as imported, false otherwise.
   */
  boolean isImported();

  /**
   * Read version from the crate descriptor and return it as a class
   * representation.
   * <p>
   * NOTE: If there is no version in the crate, it does not comply with the
   * specification.
   * 
   * @return the class representation indication the version of this crate, if
   *         available.
   */
  Optional<CrateVersion> getVersion();

  /**
   * Returns strings indicating the conformance of a crate with other
   * specifications than the RO-Crate version.
   * <p>
   * If you need the crate version too, refer to {@link #getVersion()}.
   * <p>
   * This corresponds technically to all conformsTo values, excluding the RO crate
   * version / specification.
   * 
   * @return a collection of the profiles or specifications this crate conforms
   *         to.
   */
  Collection<String> getProfiles();

  CratePreview getPreview();

  void setMetadataContext(CrateMetadataContext metadataContext);

  /**
   * Get the value of a key from the metadata context.
   * @param key the key to be searched
   * @return the value of the key, null if not found
   */
  String getMetadataContextValueOf(String key);

  /**
   * Get an immutable collection of the keys in the metadata context.
   * @return the keys in the metadata context
   */
  Set<String> getMetadataContextKeys();

  /**
   * Get an immutable map of the context.
   * @return an immutable map containing the context key-value pairs
   */
  Map<String, String> getMetadataContextPairs();

  RootDataEntity getRootDataEntity();

  void setRootDataEntity(RootDataEntity rootDataEntity);

  ContextualEntity getJsonDescriptor();

  void setJsonDescriptor(ContextualEntity jsonDescriptor);

  String getJsonMetadata();

  /**
   * Gets a data entity by its ID.
   * @param id the ID of the data entity
   * @return the DataEntity with the specified ID or null if not found
   */
  DataEntity getDataEntityById(String id);

  /**
   * Gets a data set entity by its ID.
   * @param id the ID of the data set entity
   * @return the DataSetEntity with the specified ID or empty if not found
   */
  Optional<DataSetEntity> getDataSetById(String id);

  Set<DataEntity> getAllDataEntities();

  ContextualEntity getContextualEntityById(String id);

  Set<ContextualEntity> getAllContextualEntities();

  AbstractEntity getEntityById(String id);

  /**
   * Adds a data entity to the crate.
   *
   * @param entity the DataEntity to add to this crate.
   */
  void addDataEntity(DataEntity entity);

  /**
   * Adds a data entity to the crate with a specified parent ID.
   * <p>
   * Consider using
   * @param entity the DataEntity to add to this crate.
   * @param parentId the ID of the parent entity. Must not be null.
   * @throws IllegalArgumentException if parentId is null or not found, or not a DataEntity.
   */
  void addDataEntity(DataEntity entity, String parentId) throws IllegalArgumentException;

  void addContextualEntity(ContextualEntity entity);

  void deleteEntityById(String entityId);

  void setUntrackedFiles(Collection<File> files);

  void addFromCollection(Collection<? extends AbstractEntity> entities);

  void addItemFromDataCite(String locationUrl);

  void deleteValuePairFromContext(String key);

  void deleteUrlFromContext(String url);

  Collection<File> getUntrackedFiles();

  /**
   * Automatically recognizes hierarchical file structure from DataEntity IDs
   * and connects them using hasPart relationships.
   * <p>
   * WARNING: This will not change existing hasPart relationships.
   *
   * @param addInverseRelationships if true, also adds isPartOf relationships from child to parent
   * @return result object containing information about what was processed, as well as potential errors.
   */
  HierarchyRecognitionResult createDataEntityFileStructure(boolean addInverseRelationships);

  /**
   * Automatically recognizes hierarchical file structure from DataEntity IDs
   * and connects them using hasPart relationships with fine-grained configuration.
   * <p>
   * Note: Only processes IDs that appear to be relative file paths.
   *
   * @param config configuration object specifying how the recognition should behave
   * @return result object containing information about what was processed, as well as potential errors.
   */
  HierarchyRecognitionResult createDataEntityFileStructure(HierarchyRecognitionConfig config);
}
