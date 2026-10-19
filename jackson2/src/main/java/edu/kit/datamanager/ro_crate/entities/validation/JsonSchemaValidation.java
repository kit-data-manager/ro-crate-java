package edu.kit.datamanager.ro_crate.entities.validation;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.json.jackson2.Jackson2JsonAdapter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of the entity validation strategy that uses json schema.
 *
 * <p>The neutral JSON model is converted to the native tree of the validation
 * backend at this boundary; in the split build this class moves to the
 * Jackson 2 adapter module.
 */
public class JsonSchemaValidation implements EntityValidationStrategy {

  private static final URL entitySchemaDefault = Objects.requireNonNull(
    JsonSchemaValidation.class
      .getClassLoader()
      .getResource("json_schemas/entity_schema.json")
  );
  private static final URL fieldSchemaDefault = Objects.requireNonNull(
    JsonSchemaValidation.class
      .getClassLoader()
      .getResource("json_schemas/entity_field_structure_schema.json")
  );

  private JsonSchema entitySchema;
  private JsonSchema entityFieldSchema;

  /**
   *  Default constructor that uses the default schemas.
   */
  public JsonSchemaValidation() {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(
      SpecVersion.VersionFlag.V201909
    );
    try {
      this.entitySchema = factory.getSchema(entitySchemaDefault.toURI());
      this.entityFieldSchema = factory.getSchema(fieldSchemaDefault.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * Constructor that uses custom schemas present as Json files.
   *
   * @param entitySchema schema for the entities validation.
   * @param fieldSchema schema for the field validation.
   */
  public JsonSchemaValidation(JsonValue entitySchema, JsonValue fieldSchema) {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(
      SpecVersion.VersionFlag.V201909
    );
    this.entitySchema = factory.getSchema(
      Jackson2JsonAdapter.toJackson(entitySchema)
    );
    this.entityFieldSchema = factory.getSchema(
      Jackson2JsonAdapter.toJackson(fieldSchema)
    );
  }

  @Override
  public boolean validateEntity(JsonValue entity) {
    Set<ValidationMessage> errors = this.entitySchema.validate(
      Jackson2JsonAdapter.toJackson(entity)
    );
    if (errors.size() != 0) {
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
    Set<ValidationMessage> errors = this.entityFieldSchema.validate(
      Jackson2JsonAdapter.toJackson(field)
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
