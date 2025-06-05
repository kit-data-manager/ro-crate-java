package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.util.ClasspathPropertiesVersionProvider;
import edu.kit.datamanager.ro_crate.util.VersionProvider;

import java.time.Instant;

import static edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;

/**
 * Manages provenance information for RO-Crates.
 * Handles the creation and updating of ro-crate-java entity and its actions.
 */
class ProvenanceManager {
    private record IdPrefix(String prefix) {
        public String withSuffix(String suffix) {
            return prefix + "-" + suffix;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }

    private static final IdPrefix RO_CRATE_JAVA_ID = new IdPrefix("#ro-crate-java");

    protected VersionProvider versionProvider;

    /**
     * Constructs a ProvenanceManager with the default ClasspathPropertiesVersionProvider.
     */
    public ProvenanceManager() {
        this(new ClasspathPropertiesVersionProvider());
    }

    /**
     * Constructs a ProvenanceManager with a specified VersionProvider.
     *
     * @param versionProvider The VersionProvider to use for retrieving the version of ro-crate-java.
     */
    public ProvenanceManager(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    public String getLibraryId() {
        return RO_CRATE_JAVA_ID.withSuffix(versionProvider.getVersion().toLowerCase());
    }

    void addProvenanceInformation(Crate crate) {
        // Determine if this is the first write
        boolean isFirstWrite = crate.getAllContextualEntities().stream().noneMatch(
                entity -> entity.getId().startsWith(RO_CRATE_JAVA_ID.toString()))
                && !crate.isImported();

        String libraryId = this.getLibraryId();

        // Create action entity first
        ContextualEntity actionEntity = createActionEntity(isFirstWrite, libraryId);

        // Create or update ro-crate-java entity
        ContextualEntity roCrateJavaEntity = buildRoCrateJavaEntity(crate, actionEntity.getId(), libraryId);

        // Add entities to crate
        crate.addContextualEntity(roCrateJavaEntity);
        crate.addContextualEntity(actionEntity);
    }

    private ContextualEntity createActionEntity(boolean isFirstWrite, String libraryId) {
        return new ContextualEntityBuilder()
                .addType(isFirstWrite ? "CreateAction" : "UpdateAction")
                .addIdProperty("result", "./")
                .addProperty("startTime", Instant.now().toString())
                .addIdProperty("agent", libraryId)
                .build();
    }

    private ContextualEntity buildRoCrateJavaEntity(
            Crate crate,
            String newActionId,
            String libraryId
    ) {
        String version = this.versionProvider.getVersion();
        ContextualEntity self = crate.getAllContextualEntities().stream()
                .filter(contextualEntity -> libraryId.equals(contextualEntity.getId()))
                .findFirst()
                .orElseGet(() -> {
                            return new ContextualEntityBuilder()
                                    .setId(libraryId)
                                    .addType("SoftwareApplication")
                                    .addProperty("name", "ro-crate-java")
                                    .addProperty("url", "https://github.com/kit-data-manager/ro-crate-java")
                                    .addProperty("version", version)
                                    .addProperty("softwareVersion", version)
                                    .addProperty("license", "Apache-2.0")
                                    .addProperty("description", "A Java library for creating and manipulating RO-Crates")
                                    .addIdProperty("Action", newActionId)
                                    .build();
                        }
                );
        self.addIdProperty("Action", newActionId);
        return self;
    }
}
