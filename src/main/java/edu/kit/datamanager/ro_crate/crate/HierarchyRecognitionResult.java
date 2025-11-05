package edu.kit.datamanager.ro_crate.crate;

import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;

import java.util.*;

/**
 * Result class containing information about the automatic hierarchy recognition operation.
 * This class provides details about what was processed, created, and any issues encountered
 * during the hierarchy recognition process. Always contains complete information about the
 * operation result, including success/failure and any errors encountered.
 */
public record HierarchyRecognitionResult(
        Set<DataEntity> createdEntities,
        Map<String, Set<String>> processedRelationships,
        Set<DataEntity> skippedEntities,
        List<String> warnings,
        List<String> errors
) {
    HierarchyRecognitionResult() {
        this(new HashSet<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Whether there were no errors during the hierarchy recognition operation.
     *
     * @return true if the operation completed successfully (no errors), false otherwise.
     */
    public boolean isSuccessful() {
        return this.errors.isEmpty();
    }

    public void addSkippedEntity(DataEntity entity) {
        this.skippedEntities.add(entity);
    }

    public void addError(String errorMessage) {
        this.errors.add(errorMessage);
    }

    public void addCreatedEntity(DataSetEntity newEntity) {
        this.createdEntities.add(newEntity);
    }

    public void addProcessedRelationship(String from, String to) {
        this.processedRelationships
                .computeIfAbsent(from, k -> new HashSet<>())
                .add(to);
    }
}
