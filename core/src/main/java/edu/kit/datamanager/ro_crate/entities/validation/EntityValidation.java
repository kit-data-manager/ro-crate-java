package edu.kit.datamanager.ro_crate.entities.validation;

import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Class used for validation of entities before their creation.
 */
public class EntityValidation {

  private final EntityValidationStrategy strategy;

  public EntityValidation(EntityValidationStrategy strategy) {
    this.strategy = strategy;
  }

  public boolean entityValidation(JsonValue entity) {
    return strategy.validateEntity(entity);
  }

  public boolean fieldValidation(JsonValue entity) {
    return strategy.validateFieldOfEntity(entity);
  }
}
