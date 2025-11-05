package edu.kit.datamanager.ro_crate.crate;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.util.FileSystemUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HierarchyRecognition {
    protected final Crate crate;
    protected final HierarchyRecognitionConfig config;

    public HierarchyRecognition(Crate crate, HierarchyRecognitionConfig config) {
        this.crate = crate;
        this.config = config;
    }

    public HierarchyRecognitionResult buildHierarchy() {
        HierarchyRecognitionResult result = new HierarchyRecognitionResult();

        try {
            // Get all data entities to process
            Set<DataEntity> allEntities = this.crate.getAllDataEntities();
            allEntities.add(crate.getRootDataEntity());

            // Filter entities with file path IDs (not URLs, DOIs, etc.)
            Map<String, DataEntity> pathEntities = new HashMap<>();
            for (DataEntity entity : allEntities) {
                String id = entity.getId();
                if (FileSystemUtil.isFilePath(id)) {
                    pathEntities.put(id, entity);
                } else {
                    result.addSkippedEntity(entity);
                }
            }


            // Validate hierarchy before making changes
            if (!HierarchyRecognition.validateHierarchy(pathEntities, result)) {
                return result;
            }

            // Create missing intermediate entities if configured
            if (config.createMissingIntermediateEntities()) {
                this.createMissingIntermediateEntities(pathEntities, result);
            }

            // Clear existing relationships if configured
            if (config.removeExistingConnections()) {
                this.clearExistingRelationships(pathEntities);
            }

            // Build hierarchy relationships
            this.buildHierarchyRelationships(pathEntities, config, result);

            return result;
        } catch (Exception e) {
            result.addError(
                    "Unexpected error during hierarchy recognition: " +
                            e.getMessage()
            );
            return result;
        }
    }

    /**
     * Validates that the hierarchy is consistent (no files containing other files/folders).
     *
     * @param pathEntities map of path IDs to DataEntities
     * @param result builder to collect errors
     * @return true if valid, false if invalid hierarchy detected
     */
    protected static boolean validateHierarchy(
            Map<String, DataEntity> pathEntities,
            HierarchyRecognitionResult result
    ) {
        for (Map.Entry<String, DataEntity> entry : pathEntities.entrySet()) {
            String childId = entry.getKey();
            String parentPath = FileSystemUtil.getParentPath(childId);
            if (parentPath == null || parentPath.equals("./")) {
                continue;
            }

            // Check both with and without trailing slash since files don't have slash but folders do
            DataEntity parentEntity = pathEntities.get(parentPath);
            if (parentEntity == null) {
                parentEntity = pathEntities.get(parentPath + "/");
            }

            if (parentEntity == null) {
                continue;
            }

            // Check for invalid hierarchy: file cannot contain another file/folder
            if (parentEntity.getTypes().contains("File")) {
                result.addError(
                        "Invalid hierarchy: file '" +
                                parentEntity.getId() +
                                "' cannot contain '" +
                                childId +
                                "'"
                );
                return false;
            }
        }
        return true;
    }

    /**
     * Creates missing intermediate DataSetEntity instances for folder paths.
     *
     * @param pathEntities map of path IDs to DataEntities
     * @param result builder to collect created entities
     */
    protected void createMissingIntermediateEntities(
            Map<String, DataEntity> pathEntities,
            HierarchyRecognitionResult result
    ) {
        Set<String> missingPaths = new HashSet<>();

        // Find all missing intermediate paths
        for (String path : pathEntities.keySet()) {
            String parentPath = FileSystemUtil.getParentPath(path);
            while (parentPath != null && !parentPath.equals("./")) {
                String folderPath = parentPath + "/";
                final boolean containsParent = pathEntities.containsKey(parentPath);
                final boolean containsFolder = pathEntities.containsKey(folderPath);
                if (!containsParent && !containsFolder) {
                    missingPaths.add(folderPath);
                }
                parentPath = FileSystemUtil.getParentPath(parentPath);
            }
        }

        // Create missing DataSetEntity instances
        for (String missingPath : missingPaths) {
            DataSetEntity newEntity = new DataSetEntity.DataSetBuilder()
                    .setId(missingPath)
                    .addProperty("name", "Auto-generated folder: " + missingPath)
                    .build();

            this.crate.addDataEntity(newEntity);
            pathEntities.put(missingPath, newEntity);
            result.addCreatedEntity(newEntity);
        }
    }

    protected void buildHierarchyRelationships(
            Map<String, DataEntity> pathEntities,
            HierarchyRecognitionConfig config,
            HierarchyRecognitionResult result
    ) {
        for (Map.Entry<String, DataEntity> entry : pathEntities.entrySet()) {
            String childId = entry.getKey();
            DataEntity childEntity = entry.getValue();
            String parentPath = FileSystemUtil.getParentPath(childId);
            if (parentPath == null) {
                continue;
            }

            // Check both with and without trailing slash since files don't have slash but folders do
            DataEntity parentEntity = pathEntities.get(parentPath);
            String actualParentId = parentPath;

            if (parentEntity == null) {
                parentEntity = pathEntities.get(parentPath + "/");
                actualParentId = parentPath + "/";
            }

            if (parentEntity == null) {
                continue;
            }

            // Add hasPart relationship
            if (parentEntity instanceof DataSetEntity) {
                ((DataSetEntity) parentEntity).addToHasPart(childId);
                result.addProcessedRelationship(
                        actualParentId,
                        childId
                );
            }

            // Add isPartOf relationship if configured
            if (config.createInverseRelationships()) {
                childEntity.addProperty("isPartOf", actualParentId);
            }

            // Remove from root if it has a parent that is not root
            if (!parentPath.equals("./")) {
                this.crate.getRootDataEntity().removeFromHasPart(childId);
            }
        }
    }

    protected void clearExistingRelationships(
            Map<String, DataEntity> pathEntities
    ) {
        for (DataEntity entity : pathEntities.values()) {
            if (entity instanceof DataSetEntity) {
                ((DataSetEntity) entity).hasPart.clear();
            }
        }
    }
}
