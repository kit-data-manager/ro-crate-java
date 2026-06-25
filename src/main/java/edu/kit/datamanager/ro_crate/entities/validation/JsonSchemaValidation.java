package edu.kit.datamanager.ro_crate.entities.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of the entity validation strategy that uses json schema.
 */
public class JsonSchemaValidation implements EntityValidationStrategy {

  private static final String entitySchemaClasspath =
    "classpath:json_schemas/entity_schema.json";
  private static final String fieldSchemaClasspath =
    "classpath:json_schemas/entity_field_structure_schema.json";

  private Schema entitySchema;
  private Schema entityFieldSchema;

  /**
   *  Default constructor that uses the default schemas.
   */
  public JsonSchemaValidation() {
    // Use classpath: URI scheme - $ref resolution is automatic!
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );
    this.entitySchema = schemaRegistry.getSchema(
      SchemaLocation.of(entitySchemaClasspath)
    );
    this.entityFieldSchema = schemaRegistry.getSchema(
      SchemaLocation.of(fieldSchemaClasspath)
    );
  }

  /**
   * Constructor that uses custom schemas present as Json files.
   *
   * @param entitySchema schema for the entities validation.
   * @param fieldSchema schema for the field validation.
   */
  public JsonSchemaValidation(JsonNode entitySchema, JsonNode fieldSchema) {
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );
    this.entitySchema = schemaRegistry.getSchema(
      entitySchema.toString(),
      InputFormat.JSON
    );
    this.entityFieldSchema = schemaRegistry.getSchema(
      fieldSchema.toString(),
      InputFormat.JSON
    );
  }

  @Override
  public boolean validateEntity(JsonNode entity) {
    java.util.List<Error> errors = this.entitySchema.validate(entity);
    if (!errors.isEmpty()) {
      System.err.println(
        "This entity does not comply to the basic RO-Crate entity structure."
      );
      errors.forEach(error -> System.err.println(error.getMessage()));
      return false;
    }
    return true;
  }

  @Override
  public boolean validateFieldOfEntity(JsonNode field) {
    java.util.List<Error> errors = this.entityFieldSchema.validate(field);
    if (!errors.isEmpty()) {
      ObjectMapper objectMapper = MyObjectMapper.getMapper();
      System.err.println("The property: ");
      try {
        System.err.println(
          objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(field)
        );
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      System.err.println(
        "does not comply with the flattened structure" +
          " of the RO-Crate json document."
      );
      return false;
    }
    return true;
  }
}
