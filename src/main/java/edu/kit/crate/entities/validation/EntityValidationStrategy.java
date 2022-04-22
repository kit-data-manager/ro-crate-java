package edu.kit.crate.entities.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for the strategy of the entity validation.
 */
public interface EntityValidationStrategy {

  boolean validateEntity(JsonNode entity);

  boolean validateFieldOfEntity(JsonNode field);
}
