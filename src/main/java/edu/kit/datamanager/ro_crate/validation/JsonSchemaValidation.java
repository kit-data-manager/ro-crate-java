package edu.kit.datamanager.ro_crate.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.dialect.Dialects;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

/**
 * Validation of the crate metadata using JSON-schema.
 */
public class JsonSchemaValidation implements ValidatorStrategy {

  private static final String defaultSchemaClasspath =
    "classpath:json_schemas/default.json";
  private Schema schema;

  private void getSchema(URI schemaUri) {
    try {
      // Enable fetchRemoteResources to support file:// and http:// $ref resolution
      SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
        Dialects.getDraft201909(),
        builder -> builder.schemaLoader(loader -> loader.fetchRemoteResources())
      );

      // For bundled schemas, use classpath: URI
      String location =
        schemaUri.getScheme() == null ||
        "classpath".equals(schemaUri.getScheme())
          ? defaultSchemaClasspath
          : schemaUri.toString();

      this.schema = schemaRegistry.getSchema(SchemaLocation.of(location));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load schema: " + schemaUri, e);
    }
  }

  /**
   * Default constructor for the JSON-schema validation.
   */
  public JsonSchemaValidation() {
    // Use classpath: URI scheme - $ref resolution is automatic!
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );
    this.schema = schemaRegistry.getSchema(
      SchemaLocation.of(defaultSchemaClasspath)
    );
  }

  public JsonSchemaValidation(URI schemaUri) {
    getSchema(schemaUri);
  }

  public JsonSchemaValidation(String schema) {
    URI schemaUri = new File(schema).toURI();
    getSchema(schemaUri);
  }

  public JsonSchemaValidation(JsonNode schema) {
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(
      Dialects.getDraft201909()
    );
    this.schema = schemaRegistry.getSchema(schema.toString(), InputFormat.JSON);
  }

  @Override
  public boolean validate(Crate crate) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      final JsonNode good = objectMapper.readTree(crate.getJsonMetadata());
      java.util.List<Error> errors = this.schema.validate(good);
      if (errors.size() == 0) {
        return true;
      } else {
        System.err.println(
          "This crate does not validate against the this schema." +
            " If you haven't provided any schemas," +
            " then it does not validate against the default one."
        );
        for (var e : errors) {
          System.err.println(e.getMessage());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}
