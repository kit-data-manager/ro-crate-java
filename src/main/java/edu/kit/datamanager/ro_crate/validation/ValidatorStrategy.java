package edu.kit.datamanager.ro_crate.validation;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

import edu.kit.datamanager.ro_crate.Crate;

/**
 * Interface for the validation strategy.
 */
public interface ValidatorStrategy {
  boolean validate(Crate crate);
}
