package edu.kit.datamanager.ro_crate.entities.validation.jackson3;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import edu.kit.datamanager.ro_crate.entities.validation.EntityValidationStrategy;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.json.jackson3.Jackson3JsonAdapter;
import java.util.List;

/**
 * Implementation of the entity validation strategy that uses json schema,
 * backed by the Jackson 3 version of the networknt validator (networknt 3.x
 * API with the SchemaRegistry).
 */
public class JsonSchemaValidation implements EntityValidationStrategy {

  private static final String entitySchemaDefault =
    "classpath:json_schemas/entity_schema.json";
  private static final String fieldSchemaDefault =
    "classpath:json_schemas/entity_field_structure_schema.json";

  private Schema entitySchema;
  private Schema entityFieldSchema;

  /**
   * Default constructor that uses the default schemas.
   */
  public JsonSchemaValidation() {
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
      SpecificationVersion.DRAFT_2019_09
    );
    // the schema files use relative $refs, so they must be loaded with a
    // proper base location; the classpath: scheme resolves those correctly
    // and works both from folders and jars
    this.entitySchema = registry.getSchema(
      SchemaLocation.of(entitySchemaDefault)
    );
    this.entityFieldSchema = registry.getSchema(
      SchemaLocation.of(fieldSchemaDefault)
    );
  }

  /**
   * Constructor that uses custom schemas present as Json files.
   *
   * @param entitySchema schema for the entities validation.
   * @param fieldSchema schema for the field validation.
   */
  public JsonSchemaValidation(JsonValue entitySchema, JsonValue fieldSchema) {
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
      SpecificationVersion.DRAFT_2019_09
    );
    this.entitySchema = registry.getSchema(
      Jackson3JsonAdapter.toJackson(entitySchema)
    );
    this.entityFieldSchema = registry.getSchema(
      Jackson3JsonAdapter.toJackson(fieldSchema)
    );
  }

  @Override
  public boolean validateEntity(JsonValue entity) {
    List<Error> errors = this.entitySchema.validate(
      Jackson3JsonAdapter.toJackson(entity)
    );
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
  public boolean validateFieldOfEntity(JsonValue field) {
    List<Error> errors = this.entityFieldSchema.validate(
      Jackson3JsonAdapter.toJackson(field)
    );
    if (!errors.isEmpty()) {
      System.err.println("The property: ");
      System.err.println(field.toString());
      System.err.println(
        "does not comply with the flattened structure" +
          " of the RO-Crate json document."
      );
      return false;
    }
    return true;
  }
}
