package edu.kit.datamanager.ro_crate.entities.contextual;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

public enum ActionStatus {
    ACTIVE_ACTION_STATUS("http://schema.org/ActiveActionStatus"),
    COMPLETED_ACTION_STATUS("http://schema.org/CompletedActionStatus"),
    FAILED_ACTION_STATUS("http://schema.org/FailedActionStatus"),
    POTENTIAL_ACTION_STATUS("http://schema.org/PotentialActionStatus");

    private final String id;

    ActionStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
