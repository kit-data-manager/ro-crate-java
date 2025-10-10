package edu.kit.datamanager.ro_crate.crate;

import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import java.util.*;

/**
 * Result class containing information about the automatic hierarchy recognition operation.
 * This class provides details about what was processed, created, and any issues encountered
 * during the hierarchy recognition process. Always contains complete information about the
 * operation result, including success/failure and any errors encountered.
 */
public class HierarchyRecognitionResult {

    private final Set<DataEntity> createdEntities;
    private final Map<String, Set<String>> processedRelationships;
    private final Set<DataEntity> skippedEntities;
    private final List<String> warnings;
    private final List<String> errors;
    private final boolean successful;

    private HierarchyRecognitionResult(Builder builder) {
        this.createdEntities = Collections.unmodifiableSet(
            new HashSet<>(builder.createdEntities)
        );
        this.processedRelationships = Collections.unmodifiableMap(
            new HashMap<>(builder.processedRelationships)
        );
        this.skippedEntities = Collections.unmodifiableSet(
            new HashSet<>(builder.skippedEntities)
        );
        this.warnings = Collections.unmodifiableList(
            new ArrayList<>(builder.warnings)
        );
        this.errors = Collections.unmodifiableList(
            new ArrayList<>(builder.errors)
        );
        // If there are any errors, the operation is not successful
        this.successful = builder.successful && builder.errors.isEmpty();
    }

    /**
     * Gets the entities that were automatically created during the process.
     * These are typically intermediate folder entities that were missing.
     *
     * @return set of created entities
     */
    public Set<DataEntity> getCreatedEntities() {
        return createdEntities;
    }

    /**
     * Gets the parent-child relationships that were processed.
     * The map contains parent entity IDs as keys and sets of child entity IDs as values.
     *
     * @return map of processed relationships
     */
    public Map<String, Set<String>> getProcessedRelationships() {
        return processedRelationships;
    }

    /**
     * Gets the entities that were skipped during processing.
     * These might include entities with non-file-path IDs or entities
     * that couldn't be processed for other reasons.
     *
     * @return set of skipped entities
     */
    public Set<DataEntity> getSkippedEntities() {
        return skippedEntities;
    }

    /**
     * Gets any warnings generated during the process.
     * Warnings indicate potential issues that didn't prevent the operation
     * from completing but might need attention.
     *
     * @return list of warning messages
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Gets any errors that occurred during the process.
     * Errors indicate problems that prevented the operation from
     * completing successfully or caused it to fail.
     *
     * @return list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Indicates whether the operation completed successfully.
     * Even successful operations might have warnings or skipped entities.
     *
     * @return true if the operation completed successfully
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the total number of relationships that were established.
     *
     * @return total count of parent-child relationships
     */
    public int getTotalRelationshipsCount() {
        return processedRelationships
            .values()
            .stream()
            .mapToInt(Set::size)
            .sum();
    }

    /**
     * Checks if any entities were created during the process.
     *
     * @return true if entities were created
     */
    public boolean hasCreatedEntities() {
        return !createdEntities.isEmpty();
    }

    /**
     * Checks if any entities were skipped during the process.
     *
     * @return true if entities were skipped
     */
    public boolean hasSkippedEntities() {
        return !skippedEntities.isEmpty();
    }

    /**
     * Checks if any warnings were generated during the process.
     *
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Checks if any errors occurred during the process.
     *
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Creates a new builder for constructing HierarchyRecognitionResult instances.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating HierarchyRecognitionResult instances.
     */
    public static class Builder {

        private final Set<DataEntity> createdEntities = new HashSet<>();
        private final Map<String, Set<String>> processedRelationships =
            new HashMap<>();
        private final Set<DataEntity> skippedEntities = new HashSet<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private boolean successful = true;

        /**
         * Adds an entity that was created during the process.
         *
         * @param entity the created entity
         * @return this builder
         */
        public Builder addCreatedEntity(DataEntity entity) {
            this.createdEntities.add(entity);
            return this;
        }

        /**
         * Adds multiple entities that were created during the process.
         *
         * @param entities the created entities
         * @return this builder
         */
        public Builder addCreatedEntities(Collection<DataEntity> entities) {
            this.createdEntities.addAll(entities);
            return this;
        }

        /**
         * Adds a processed parent-child relationship.
         *
         * @param parentId the parent entity ID
         * @param childId the child entity ID
         * @return this builder
         */
        public Builder addProcessedRelationship(
            String parentId,
            String childId
        ) {
            this.processedRelationships.computeIfAbsent(parentId, k ->
                new HashSet<>()
            ).add(childId);
            return this;
        }

        /**
         * Adds multiple processed relationships for a parent.
         *
         * @param parentId the parent entity ID
         * @param childIds the child entity IDs
         * @return this builder
         */
        public Builder addProcessedRelationships(
            String parentId,
            Collection<String> childIds
        ) {
            this.processedRelationships.computeIfAbsent(parentId, k ->
                new HashSet<>()
            ).addAll(childIds);
            return this;
        }

        /**
         * Adds an entity that was skipped during processing.
         *
         * @param entity the skipped entity
         * @return this builder
         */
        public Builder addSkippedEntity(DataEntity entity) {
            this.skippedEntities.add(entity);
            return this;
        }

        /**
         * Adds multiple entities that were skipped during processing.
         *
         * @param entities the skipped entities
         * @return this builder
         */
        public Builder addSkippedEntities(Collection<DataEntity> entities) {
            this.skippedEntities.addAll(entities);
            return this;
        }

        /**
         * Adds a warning message.
         *
         * @param warning the warning message
         * @return this builder
         */
        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }

        /**
         * Adds multiple warning messages.
         *
         * @param warnings the warning messages
         * @return this builder
         */
        public Builder addWarnings(Collection<String> warnings) {
            this.warnings.addAll(warnings);
            return this;
        }

        /**
         * Adds an error message and marks the operation as unsuccessful.
         *
         * @param error the error message
         * @return this builder
         */
        public Builder addError(String error) {
            this.errors.add(error);
            this.successful = false;
            return this;
        }

        /**
         * Adds multiple error messages and marks the operation as unsuccessful.
         *
         * @param errors the error messages
         * @return this builder
         */
        public Builder addErrors(Collection<String> errors) {
            this.errors.addAll(errors);
            if (!errors.isEmpty()) {
                this.successful = false;
            }
            return this;
        }

        /**
         * Sets whether the operation was successful.
         *
         * @param successful true if successful
         * @return this builder
         */
        public Builder setSuccessful(boolean successful) {
            this.successful = successful;
            return this;
        }

        /**
         * Builds the result object.
         *
         * @return the constructed HierarchyRecognitionResult
         */
        public HierarchyRecognitionResult build() {
            return new HierarchyRecognitionResult(this);
        }
    }

    @Override
    public String toString() {
        return (
            "HierarchyRecognitionResult{" +
            "successful=" +
            successful +
            ", createdEntities=" +
            createdEntities.size() +
            ", processedRelationships=" +
            getTotalRelationshipsCount() +
            ", skippedEntities=" +
            skippedEntities.size() +
            ", warnings=" +
            warnings.size() +
            ", errors=" +
            errors.size() +
            '}'
        );
    }
}
