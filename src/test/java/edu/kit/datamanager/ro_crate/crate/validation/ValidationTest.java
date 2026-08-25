package edu.kit.datamanager.ro_crate.crate.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.WorkflowEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class ValidationTest {

  @Test
  void jsonSchemaValidationTest() throws IOException, URISyntaxException {
    Crate crate = new RoCrate.RoCrateBuilder(
      "workflowCrate",
      "this is a test",
      "2024",
      "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
    )
      .addDataEntity(
        new WorkflowEntity.WorkflowEntityBuilder()
          .setId("https://www.example.com/entity")
          .build()
      )
      .build();

    InputStream inputStream = ValidationTest.class.getResourceAsStream(
      "/crates/validation/workflowschema.json"
    );
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    Validator validator = new Validator(new JsonSchemaValidation(expectedJson));
    assertTrue(validator.validate(crate));
    URL schemaUrl = Objects.requireNonNull(
      ValidationTest.class.getResource("/crates/validation/workflowschema.json")
    );
    String schemaPath = schemaUrl.getPath();
    // test with string file location
    validator = new Validator(new JsonSchemaValidation(schemaPath));
    assertTrue(validator.validate(crate));
    // test with URI schema
    validator = new Validator(new JsonSchemaValidation(schemaUrl.toURI()));
    assertTrue(validator.validate(crate));

    crate.deleteEntityById("https://www.example.com/entity");
    // crate should not match this schema
    assertFalse(validator.validate(crate));
  }

  @Test
  void customSchemaWithCrossFileRefTest()
    throws IOException, URISyntaxException {
    // Test that custom schemas with cross-file $ref references load correctly.
    // If $ref resolution fails, the constructor would throw an exception.
    URL parentSchemaUrl = Objects.requireNonNull(
      ValidationTest.class.getResource(
        "/json_schemas/custom-parent-schema.json"
      )
    );

    // This constructor call will fail if $ref to custom-child-schema.json can't be resolved
    JsonSchemaValidation schemaValidation = new JsonSchemaValidation(
      parentSchemaUrl.toURI()
    );
    Validator validator = new Validator(schemaValidation);

    // Create a crate - validation will run but the crate structure won't match
    // our custom schema (which expects {"person": {"name": "..."}}).
    // The key assertion is that schema LOADING succeeded (no exception above).
    RoCrate crate = new RoCrate.RoCrateBuilder(
      "Test",
      "Desc",
      "2024",
      "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
    ).build();

    // Validate returns false because crate structure doesn't match custom schema,
    // but the important thing is that validation RUNS without errors
    boolean result = validator.validate(crate);

    // We expect false because our crate doesn't have the "person" field
    // that the custom schema requires. This proves:
    // 1. Schema loaded successfully ($ref was resolved)
    // 2. Validation executed against the loaded schema
    assertFalse(
      result,
      "Crate should not validate against custom schema (missing 'person' field)"
    );
  }
}
