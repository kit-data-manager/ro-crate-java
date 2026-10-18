package edu.kit.datamanager.ro_crate.validation.jackson3;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.json.jackson3.Jackson3JsonAdapter;
import edu.kit.datamanager.ro_crate.validation.ValidatorStrategy;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Validation of the crate metadata using JSON-schema, backed by the Jackson 3
 * version of the networknt validator (networknt 3.x API with the
 * SchemaRegistry).
 */
public class JsonSchemaValidation implements ValidatorStrategy {

  private static final String defaultSchema = "json_schemas/default.json";
  private Schema schema;

  private void getSchema(URI schemaUri) {
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
      SpecificationVersion.DRAFT_2019_09
    );
    this.schema = registry.getSchema(SchemaLocation.of(schemaUri.toString()));
  }

  /**
   * Default constructor for the JSON-schema validation.
   */
  public JsonSchemaValidation() {
    // the classpath: scheme resolves relative $refs correctly and works
    // both from folders and jars
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
      SpecificationVersion.DRAFT_2019_09
    );
    this.schema = registry.getSchema(
      SchemaLocation.of("classpath:" + defaultSchema)
    );
  }

  public JsonSchemaValidation(URI schemaUri) {
    getSchema(schemaUri);
  }

  public JsonSchemaValidation(String schema) {
    URI schemaUri = new File(schema).toURI();
    getSchema(schemaUri);
  }

  public JsonSchemaValidation(JsonValue schema) {
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
      SpecificationVersion.DRAFT_2019_09
    );
    this.schema = registry.getSchema(Jackson3JsonAdapter.toJackson(schema));
  }

  @Override
  public boolean validate(Crate crate) {
    try {
      final JsonValue good = Json.parse(crate.getJsonMetadata());
      List<Error> errors = this.schema.validate(
        Jackson3JsonAdapter.toJackson(good)
      );
      if (errors.isEmpty()) {
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
