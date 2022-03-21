package edu.kit.crate.entities.validation;


import com.fasterxml.jackson.databind.JsonNode;

public class EntityValidation {

  private IEntityValidationStrategy strategy;

  public EntityValidation(IEntityValidationStrategy strategy) {
    this.strategy = strategy;
  }

  public boolean entityValidation(JsonNode entity) {
    return strategy.validateEntity(entity);
  }

  public boolean fieldValidation(JsonNode entity) {
    return strategy.validateFieldOfEntity(entity);
  }
}
