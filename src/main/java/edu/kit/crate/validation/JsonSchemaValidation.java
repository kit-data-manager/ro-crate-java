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
import java.util.Objects;
import java.util.Set;

public class JsonSchemaValidation implements IValidatorStrategy {
  private static final String defaultSchema = "json_schemas/default.json";
  private JsonNode schema;

  public JsonSchemaValidation() {
    String str = Objects.requireNonNull(getClass().getClassLoader().getResource(defaultSchema)).getFile();
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      this.schema = objectMapper.readTree(new File(str));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public JsonSchemaValidation(String schema) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      this.schema = objectMapper.readTree(new File(schema));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public JsonSchemaValidation(JsonNode schema) {
    this.schema = schema;
  }

  @Override
  public boolean validate(IROCrate crate) {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    try {
      final JsonNode good = objectMapper.readTree(crate.getJsonMetadata());
      JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
      JsonSchema jsonSchema = factory.getSchema(this.schema);
      Set<ValidationMessage> errors = jsonSchema.validate(good);
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
