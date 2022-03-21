package edu.kit.crate.entities.validation;

import com.fasterxml.jackson.databind.JsonNode;

public interface IEntityValidationStrategy {
  boolean validateEntity(JsonNode entity);
  boolean validateFieldOfEntity(JsonNode field);
}
