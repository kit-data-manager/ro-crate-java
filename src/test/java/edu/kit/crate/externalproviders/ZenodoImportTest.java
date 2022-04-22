package edu.kit.crate.externalproviders;

import edu.kit.crate.RoCrate;
import edu.kit.crate.externalproviders.dataentities.ImportFromZenodo;
import edu.kit.crate.validation.JsonSchemaValidation;
import edu.kit.crate.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZenodoImportTest {

  @Test
  void testImportingNewCrate() {
    String url = "https://zenodo.org/api/records/6411574";
    var crate = ImportFromZenodo.createCrateWithItem(url, "name", "description");
    Validator validator = new Validator(new JsonSchemaValidation());
    assertTrue(validator.validate(crate));
  }

  @Test
  void testImportToExistingCrate() {
    String url = "https://zenodo.org/api/records/6411574";
    var crate = new RoCrate.RoCrateBuilder("name", "description").build();
    ImportFromZenodo.addZenodoToCrate(url, crate);
    Validator validator = new Validator(new JsonSchemaValidation());
    assertTrue(validator.validate(crate));
  }
}
