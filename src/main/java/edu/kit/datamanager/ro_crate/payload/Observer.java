package edu.kit.datamanager.ro_crate.payload;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Interface for the observer pattern used with the deletion of entities.
 */
public interface Observer {
  void update(String entityId);
}
