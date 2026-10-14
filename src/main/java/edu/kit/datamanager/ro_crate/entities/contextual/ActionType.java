package edu.kit.datamanager.ro_crate.entities.contextual;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Enumeration class representing action types.
 * @author sabrinechelbi
 */
public enum ActionType {

    CREATE("CreateAction"),
    UPDATE("UpdateAction");

    private final String name;

    ActionType(String name) {
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
}
