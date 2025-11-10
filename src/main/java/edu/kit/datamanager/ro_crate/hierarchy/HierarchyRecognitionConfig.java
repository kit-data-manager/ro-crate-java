package edu.kit.datamanager.ro_crate.hierarchy;

/**
 * Configuration class for automatic hierarchy recognition.
 * This class provides control over how the hierarchy recognition.
 *
 * <ul>
 *     <li>createMissingIntermediateEntities: Whether missing intermediate folder entities should be automatically created. Default: false</li>
 *     <li>createInverseRelationships: Whether isPartOf relationships should be added in addition to hasPart. Default: false</li>
 *     <li>removeExistingConnections: Whether hasPart relationships should be added (false) or remove existing relations in beforehand (true). Default: false</li>
 * </ul>
 */
public record HierarchyRecognitionConfig(
        boolean createMissingIntermediateEntities,
        boolean createInverseRelationships,
        boolean removeExistingConnections
) {
    /**
     * Creates a new configuration with default values.
     * <p>
     * Default values:
     * <ul>
     *   <li>createMissingIntermediateEntities: false</li>
     *   <li>createInverseRelationships: false</li>
     *   <li>removeExistingConnections: false</li>
     * </ul>
     */
    public HierarchyRecognitionConfig() {
        this(false, false, false);
    }

    public HierarchyRecognitionConfig withCreateMissingIntermediateEntities(boolean value) {
        return new HierarchyRecognitionConfig(value, this.createInverseRelationships, this.removeExistingConnections);
    }

    public HierarchyRecognitionConfig withSetInverseRelationships(boolean value) {
        return new HierarchyRecognitionConfig(this.createMissingIntermediateEntities, value, this.removeExistingConnections);
    }

    public HierarchyRecognitionConfig withRemoveExistingConnections(boolean value) {
        return new HierarchyRecognitionConfig(this.createMissingIntermediateEntities, this.createInverseRelationships, value);
    }
}
