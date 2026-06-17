package edu.kit.datamanager.ro_crate.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Test to verify the migration document claim that
 * "classpath resources will still be automatically loaded"
 * when using the classpath: URI scheme.
 */
class ClasspathResourceLoadingTest {

  /**
   * Note: file:// URIs do NOT support automatic $ref resolution.
   * Our implementation handles this by scanning the directory for sibling schemas.
   */

  @Test
  void testClasspathUriSchemeRefResolution() {
    // This test verifies if using classpath: URI scheme enables
    // automatic $ref resolution as the migration document suggests

    // Create SchemaRegistry WITHOUT registering any schemas
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
      // NOTE: We're NOT calling .schemas() to register anything
    );

    // Try to load schema using classpath: URI scheme
    assertDoesNotThrow(() -> {
      Schema schema = schemaRegistry.getSchema(
        SchemaLocation.of(
          "classpath:json_schemas/classpath-ref-test-parent.json"
        )
      );

      // If we got here, the schema loaded
      // Now try to validate data that requires the child schema via $ref
      var validData = "{\"person\": {\"name\": \"John\"}}";
      var errors = schema.validate(validData, InputFormat.JSON);

      // Should have no errors for valid data if $ref was resolved
      assertTrue(
        errors.isEmpty(),
        "Valid data should pass validation. Errors: " + errors
      );
    }, "Schema with $ref should work using classpath: URI scheme without manual registration");
  }
}
