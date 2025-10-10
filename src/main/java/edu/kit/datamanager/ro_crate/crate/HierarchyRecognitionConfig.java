package edu.kit.datamanager.ro_crate.crate;

/**
 * Configuration class for automatic hierarchy recognition functionality.
 * This class provides fine-grained control over how the hierarchy recognition
 * algorithm behaves using a fluent interface with setter methods.
 */
public class HierarchyRecognitionConfig {

    /**
     * Whether missing intermediate folder entities should be automatically created.
     * <p>
     * Default: false (only connect existing entities)
     */
    public boolean createMissingIntermediateEntities = false;

    /**
     * Whether isPartOf relationships should be added in addition to hasPart.
     * <p>
     * Default: false (only add hasPart relationships)
     */
    public boolean setInverseRelationships = false;

    /**
     * Whether hasPart relationships should be added (false)
     * or remove existing relations in beforehand (true).
     * <p>
     * Default: false (keep relations)
     */
    public boolean removeExistingConnections = false;

    /**
     * Creates a new configuration with default values.
     */
    public HierarchyRecognitionConfig() {
        // All defaults are set via field initializers
    }

    /**
     * Sets whether missing intermediate folder entities should be automatically created.
     *
     * @param create true to create missing DataSetEntity instances for intermediate folders
     * @return this configuration object for method chaining
     */
    public HierarchyRecognitionConfig createMissingIntermediateEntities(
        boolean create
    ) {
        this.createMissingIntermediateEntities = create;
        return this;
    }

    /**
     * Sets whether isPartOf relationships should be added in addition to hasPart.
     *
     * @param addIsPartOf true to add bidirectional relationships
     * @return this configuration object for method chaining
     */
    public HierarchyRecognitionConfig setInverseRelationships(
        boolean addIsPartOf
    ) {
        this.setInverseRelationships = addIsPartOf;
        return this;
    }

    /**
     * Whether hasPart relationships should be added (false)
     * or remove existing relations in beforehand (true).
     *
     * @param removeExistingConnections true to remove existing connections
     * @return this configuration object for method chaining
     */
    public HierarchyRecognitionConfig removeExistingConnections(
        boolean removeExistingConnections
    ) {
        this.removeExistingConnections = removeExistingConnections;
        return this;
    }

    /**
     * Creates a configuration with default sensible values.
     * @return default configuration
     */
    public static HierarchyRecognitionConfig defaultConfig() {
        return new HierarchyRecognitionConfig();
    }

    @Override
    public String toString() {
        return (
            "HierarchyRecognitionConfig{" +
            "createMissingIntermediateEntities=" +
            createMissingIntermediateEntities +
            ", addIsPartOfRelationships=" +
            setInverseRelationships +
            '}'
        );
    }
}
