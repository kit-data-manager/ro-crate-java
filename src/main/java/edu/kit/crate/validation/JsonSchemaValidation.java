package edu.kit.crate.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import edu.kit.crate.IROCrate;
import edu.kit.crate.objectmapper.MyObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Set;

public class JsonSchemaValidation implements IValidatorStrategy {

  private static final String defaultSchema = "json_schemas/default.json";
  private JsonSchema schema;

  private void getSchema(URI schemaURI) {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    this.schema = factory.getSchema(schemaURI);
  }

  public JsonSchemaValidation() {
    try {
      URI schemaURI = Objects.requireNonNull(getClass().getClassLoader().getResource(defaultSchema)).toURI();
      getSchema(schemaURI);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public JsonSchemaValidation(URI schemaURI) {
    getSchema(schemaURI);
  }

  public JsonSchemaValidation(String schema) {
    URI schemaURI = new File(schema).toURI();
    getSchema(schemaURI);
  }

  public JsonSchemaValidation(JsonNode schema) {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    this.schema = factory.getSchema(schema);
  }

  @Override
  public boolean validate(IROCrate crate) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      final JsonNode good = objectMapper.readTree(crate.getJsonMetadata());
      Set<ValidationMessage> errors = this.schema.validate(good);
      if (errors.size() == 0) {
        return true;
      } else {
        System.err.println("This crate does not validate against the this schema. If you haven't provided any schemas, then it does not validate against the default one.");
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
