package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity.ContextualEntityBuilder;

/**
 * Manages provenance information for RO-Crates.
 * Handles the creation and updating of ro-crate-java entity and its actions.
 */
class ProvenanceManager {
    private static final String RO_CRATE_JAVA_ID = "#ro-crate-java";

    void addProvenanceInformation(Crate crate) {
        // Determine if this is the first write
        boolean isFirstWrite = !crate.getJsonMetadata().contains(RO_CRATE_JAVA_ID) && !crate.isImported();

        // Create action entity first
        String actionId = "#" + UUID.randomUUID();
        ContextualEntity actionEntity = createActionEntity(actionId, isFirstWrite);

        // Create or update ro-crate-java entity
        ContextualEntity roCrateJavaEntity = buildRoCrateJavaEntity(crate, actionId, isFirstWrite);

        // Add entities to crate
        crate.addContextualEntity(roCrateJavaEntity);
        crate.addContextualEntity(actionEntity);
    }

    private ContextualEntity createActionEntity(String actionId, boolean isFirstWrite) {
        return new ContextualEntityBuilder()
            .setId(actionId)
            .addType(isFirstWrite ? "CreateAction" : "UpdateAction")
            .addProperty("startTime", Instant.now().toString())
            .addIdProperty("agent", RO_CRATE_JAVA_ID)
            .build();
    }

    private ContextualEntity buildRoCrateJavaEntity(
            Crate crate,
            String newActionId,
            boolean isFirstWrite
    ) {
        ContextualEntity self = crate.getAllContextualEntities().stream()
                .filter(contextualEntity -> RO_CRATE_JAVA_ID.equals(contextualEntity.getId()))
                .findFirst()
                .orElseGet(() ->
                        new ContextualEntityBuilder()
                                .setId(RO_CRATE_JAVA_ID)
                                .addType("SoftwareApplication")
                                .addProperty("name", "ro-crate-java")
                                .addProperty("url", "https://github.com/kit-data-manager/ro-crate-java")
                                // TODO read software version and version from gradle (write into resources properties file when building and read it from there)
                                .addProperty("version", "1.0.0")
                                .addProperty("softwareVersion", "1.0.0")
                                .addProperty("license", "Apache-2.0")
                                .addProperty("description", "A Java library for creating and manipulating RO-Crates")
                                .addIdProperty("Action", newActionId)
                                .build()
                );
        self.addIdProperty("Action", newActionId);
        return self;
    }
}
