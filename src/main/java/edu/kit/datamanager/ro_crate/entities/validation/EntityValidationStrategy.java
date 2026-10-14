package edu.kit.datamanager.ro_crate.entities.validation;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;


/**
 * Interface for the strategy of the entity validation.
 */
public interface EntityValidationStrategy {

  boolean validateEntity(JsonValue entity);

  boolean validateFieldOfEntity(JsonValue field);
}
