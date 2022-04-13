package edu.kit.crate.crate.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.IROCrate;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.data.WorkflowEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.validation.JsonSchemaValidation;
import edu.kit.crate.validation.Validator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTest {

  @Test
  void jsonSchemaValidationTest() throws IOException, URISyntaxException {
    IROCrate crate = new ROCrate.ROCrateBuilder("workflowCrate", "this is a test")
        .addDataEntity(
            new WorkflowEntity.WorkflowEntityBuilder()
                .setId("https://www.example.com/entity")
                .build()
        )
        .build();

    InputStream inputStream =
        ValidationTest.class.getResourceAsStream("/crates/validation/workflowschema.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);

    Validator validator = new Validator(new JsonSchemaValidation(expectedJson));
    assertTrue(validator.validate(crate));
    URL schemaUrl = Objects.requireNonNull(ValidationTest.class.getResource("/crates/validation/workflowschema.json"));
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
}
