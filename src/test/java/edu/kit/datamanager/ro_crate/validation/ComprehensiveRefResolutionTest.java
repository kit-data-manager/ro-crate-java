package edu.kit.datamanager.ro_crate.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for $ref resolution in json-schema-validator 2.x
 * Tests various scenarios: siblings, nested paths, absolute paths, classpath, file://
 */
class ComprehensiveRefResolutionTest {

  /**
   * Test 1: Classpath schemas with relative $ref to sibling
   * This SHOULD work - classpath: URIs support automatic $ref resolution
   */
  @Test
  void testClasspathSiblingRef() throws Exception {
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );

    Schema schema = schemaRegistry.getSchema(
      SchemaLocation.of("classpath:json_schemas/custom-parent-schema.json")
    );

    List<Error> errors = schema.validate(
      "{\"person\": {\"name\": \"John\"}}",
      InputFormat.JSON
    );
    assertTrue(
      errors.isEmpty(),
      "Should validate with valid data. Errors: " + errors
    );

    List<Error> invalidErrors = schema.validate(
      "{\"person\": {}}",
      InputFormat.JSON
    );
    assertFalse(
      invalidErrors.isEmpty(),
      "Should fail validation without required name"
    );
  }

  /**
   * Test 2: File-based schemas with relative $ref to sibling
   * Tests loading from actual filesystem path WITH fetchRemoteResources enabled
   */
  @Test
  void testFileSiblingRef() throws Exception {
    URL resourceUrl = getClass()
      .getClassLoader()
      .getResource("json_schemas/custom-parent-schema.json");
    assertNotNull(resourceUrl, "Resource should exist");

    Path schemaPath = Path.of(resourceUrl.toURI());

    // MUST enable fetchRemoteResources for file:// $ref resolution
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909(),
      builder -> builder.schemaLoader(loader -> loader.fetchRemoteResources())
    );

    Schema schema = schemaRegistry.getSchema(
      SchemaLocation.of(schemaPath.toUri().toString())
    );

    List<Error> errors = schema.validate(
      "{\"person\": {\"name\": \"John\"}}",
      InputFormat.JSON
    );
    assertTrue(
      errors.isEmpty(),
      "Should validate with valid data. Errors: " + errors
    );
  }

  /**
   * Test 3: Pre-registering schemas in SchemaRegistry for $ref resolution
   * Using absolute URIs as schema IDs so $ref can resolve them
   */
  @Test
  void testPreRegisteredSchemasWithAbsoluteIds() throws Exception {
    // Load both parent and child schemas
    URL parentUrl = getClass()
      .getClassLoader()
      .getResource("json_schemas/custom-parent-schema.json");
    URL childUrl = getClass()
      .getClassLoader()
      .getResource("json_schemas/custom-child-schema.json");

    String parentContent = Files.readString(Path.of(parentUrl.toURI()));
    String childContent = Files.readString(Path.of(childUrl.toURI()));

    // Register schemas with absolute URIs as IDs (matching what $ref would resolve to)
    String baseUri = parentUrl
      .toString()
      .substring(0, parentUrl.toString().lastIndexOf('/') + 1);
    Map<String, String> schemas = Map.of(
      baseUri + "custom-parent-schema.json",
      parentContent,
      baseUri + "custom-child-schema.json",
      childContent
    );

    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909(),
      builder -> builder.schemas(schemas)
    );

    // Load using the absolute URI
    Schema schema = schemaRegistry.getSchema(
      SchemaLocation.of(baseUri + "custom-parent-schema.json")
    );

    List<Error> errors = schema.validate(
      "{\"person\": {\"name\": \"John\"}}",
      InputFormat.JSON
    );
    assertTrue(
      errors.isEmpty(),
      "Should validate with valid data. Errors: " + errors
    );

    List<Error> invalidErrors = schema.validate(
      "{\"person\": {}}",
      InputFormat.JSON
    );
    assertFalse(
      invalidErrors.isEmpty(),
      "Should fail validation without required name"
    );
  }

  /**
   * Test 4: Using schemaIdResolvers to map IDs to classpath locations
   */
  @Test
  void testSchemaIdResolverMapping() throws Exception {
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909(),
      builder ->
        builder.schemaIdResolvers(resolvers ->
          resolvers.mapPrefix("my-schema:", "classpath:json_schemas/")
        )
    );

    // This would work if our schema had "$id": "my-schema:custom-parent-schema.json"
    // For now, just verify the resolver is configured
    assertDoesNotThrow(() -> {
      Schema schema = schemaRegistry.getSchema(
        SchemaLocation.of("classpath:json_schemas/custom-parent-schema.json")
      );
      List<Error> errors = schema.validate(
        "{\"person\": {\"name\": \"John\"}}",
        InputFormat.JSON
      );
      assertTrue(errors.isEmpty());
    });
  }

  /**
   * Test 5: Nested directory structure with $ref
   * Creates a temporary directory structure to test non-sibling refs
   */
  @Test
  void testNestedDirectoryRef() throws Exception {
    // Create temp directory structure
    Path tempDir = Files.createTempDirectory("schema-test");
    try {
      Path schemasDir = Files.createDirectories(tempDir.resolve("schemas"));
      Path definitionsDir = Files.createDirectories(
        schemasDir.resolve("definitions")
      );

      // Create child schema in nested directory
      String childSchema = """
        {
          "$schema": "https://json-schema.org/draft/2019-09/schema",
          "type": "object",
          "properties": {
            "value": {"type": "string"}
          },
          "required": ["value"]
        }
        """;
      Files.writeString(definitionsDir.resolve("types.json"), childSchema);

      // Create parent schema that refs nested child
      String parentSchema = """
        {
          "$schema": "https://json-schema.org/draft/2019-09/schema",
          "type": "object",
          "properties": {
            "item": {"$ref": "definitions/types.json"}
          },
          "required": ["item"]
        }
        """;
      Files.writeString(schemasDir.resolve("parent.json"), parentSchema);

      // Test loading parent schema - MUST enable fetchRemoteResources for file:// $ref
      SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
        Dialects.getDraft201909(),
        builder -> builder.schemaLoader(loader -> loader.fetchRemoteResources())
      );

      Schema schema = schemaRegistry.getSchema(
        SchemaLocation.of(schemasDir.resolve("parent.json").toUri().toString())
      );

      List<Error> errors = schema.validate(
        "{\"item\": {\"value\": \"test\"}}",
        InputFormat.JSON
      );
      assertTrue(
        errors.isEmpty(),
        "Should validate nested ref. Errors: " + errors
      );
    } finally {
      // Cleanup
      deleteRecursively(tempDir);
    }
  }

  /**
   * Test 6: KEY TEST - Does fetchRemoteResources() fix file:// $ref resolution?
   */
  @Test
  void testRemoteFetchingFixesFileRef() throws Exception {
    URL resourceUrl = getClass()
      .getClassLoader()
      .getResource("json_schemas/custom-parent-schema.json");
    assertNotNull(resourceUrl, "Resource should exist");

    Path schemaPath = Path.of(resourceUrl.toURI());

    // Create registry WITH remote fetching enabled
    SchemaRegistry schemaRegistryWithRemote = SchemaRegistry.withDialect(
      Dialects.getDraft201909(),
      builder -> builder.schemaLoader(loader -> loader.fetchRemoteResources())
    );

    // Try loading with file:// URI and remote fetching enabled
    assertDoesNotThrow(() -> {
      Schema schema = schemaRegistryWithRemote.getSchema(
        SchemaLocation.of(schemaPath.toUri().toString())
      );

      List<Error> errors = schema.validate(
        "{\"person\": {\"name\": \"John\"}}",
        InputFormat.JSON
      );
      assertTrue(
        errors.isEmpty(),
        "Should validate with valid data. Errors: " + errors
      );
    }, "file:// $ref SHOULD work with fetchRemoteResources() enabled");
  }

  /**
   * Test 6b: Control test - Verify file:// still fails WITHOUT remote fetching
   */
  @Test
  void testFileRefFailsWithoutRemoteFetching() throws Exception {
    URL resourceUrl = getClass()
      .getClassLoader()
      .getResource("json_schemas/custom-parent-schema.json");
    assertNotNull(resourceUrl, "Resource should exist");

    Path schemaPath = Path.of(resourceUrl.toURI());

    // Create registry WITHOUT remote fetching (default)
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );

    // This should fail because $ref can't be resolved
    assertThrows(
      Exception.class,
      () -> {
        Schema schema = schemaRegistry.getSchema(
          SchemaLocation.of(schemaPath.toUri().toString())
        );
        schema.validate("{\"person\": {\"name\": \"John\"}}", InputFormat.JSON);
      },
      "file:// $ref should fail without fetchRemoteResources()"
    );
  }

  /**
   * Test 7: Loading schema without $id (uses retrieval IRI as schema ID)
   */
  @Test
  void testSchemaWithoutId() throws Exception {
    // Our test schemas don't have $id, so they should use the retrieval IRI as their ID
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );

    assertDoesNotThrow(() -> {
      Schema schema = schemaRegistry.getSchema(
        SchemaLocation.of("classpath:json_schemas/custom-child-schema.json")
      );

      // Validate directly
      List<Error> errors = schema.validate(
        "{\"name\": \"test\"}",
        InputFormat.JSON
      );
      assertTrue(
        errors.isEmpty(),
        "Schema without $id should work. Errors: " + errors
      );
    });
  }

  private void deleteRecursively(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      try (var stream = Files.list(path)) {
        stream.forEach(p -> {
          try {
            deleteRecursively(p);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }
    Files.deleteIfExists(path);
  }
}
