package edu.kit.datamanager.ro_crate.writer;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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

        // Add entities to crate in correct order (referenced entity first)
        crate.addContextualEntity(roCrateJavaEntity);
        crate.addContextualEntity(actionEntity);
    }

    private ContextualEntity createActionEntity(String actionId, boolean isFirstWrite) {
        return new ContextualEntity.ContextualEntityBuilder()
            .setId(actionId)
            .addType(isFirstWrite ? "CreateAction" : "UpdateAction")
            .addProperty("startTime", Instant.now().toString())
            .addIdProperty("agent", RO_CRATE_JAVA_ID)
            .build();
    }

    private ContextualEntity buildRoCrateJavaEntity(Crate crate, String newActionId, boolean isFirstWrite) {
        ContextualEntity.ContextualEntityBuilder builder = new ContextualEntity.ContextualEntityBuilder()
            .setId(RO_CRATE_JAVA_ID)
            .addType("SoftwareApplication")
            .addProperty("name", "ro-crate-java")
            .addProperty("url", "https://github.com/kit-data-manager/ro-crate-java")
            // TODO read software version and version from gradle (write into resources properties file when building and read it from there)
            .addProperty("version", "1.0.0")
            .addProperty("softwareVersion", "1.0.0")
            .addProperty("license", "Apache-2.0")
            .addProperty("description", "A Java library for creating and manipulating RO-Crates");

        if (isFirstWrite) {
            builder.addIdProperty("Action", newActionId);
        } else {
            Collection<ContextualEntity> entities = crate.getAllContextualEntities();
            for (ContextualEntity entity : entities) {
                if (RO_CRATE_JAVA_ID.equals(entity.getId())) {
                    addActionToBuilder(builder, entity, newActionId);
                    break;
                }
            }
        }

        return builder.build();
    }

    private void addActionToBuilder(
            ContextualEntity.ContextualEntityBuilder builder,
            ContextualEntity existingEntity,
            String newActionId
    ) {
        Object existingAction = existingEntity.getProperty("Action");
        if (existingAction == null) {
            builder.addIdProperty("action", newActionId);
            return;
        }

        // When there are existing actions, we need to preserve them
        if (existingAction instanceof Map) {
            // Single previous action (as a Map containing @id)
            String existingActionId = ((Map<?, ?>) existingAction).get("@id").toString();
            builder.addIdProperty("Action", "#" + existingActionId);
            builder.addIdProperty("Action", newActionId);
        } else if (existingAction instanceof Collection<?> oldActions) {
            // Multiple previous actions -> Add all existing actions
            oldActions.stream()
                .map(action -> ((Map<?, ?>) action).get("@id").toString())
                .map(id -> !id.startsWith("#") ? "#" + id : id)
                .forEach(id -> builder.addIdProperty("action", id));
            // Add the new action
            builder.addIdProperty("Action", newActionId);
        } else {
            // Unexpected format, just add the new action
            builder.addIdProperty("Action", newActionId);
        }
    }
}
