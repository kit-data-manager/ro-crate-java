package edu.kit.datamanager.ro_crate.externalproviders;

import org.junit.jupiter.api.Test;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.externalproviders.dataentities.ImportFromZenodo;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;

public class ZenodoImportTest {

  @Disabled("Disabled while issues are being fixed!")
  @Test
  void testImportingNewCrate() {
    String url = "https://zenodo.org/api/records/6411574";
    var crate = ImportFromZenodo.createCrateWithItem(url, "name", "description", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/");
    Validator validator = new Validator(new JsonSchemaValidation());
    assertTrue(validator.validate(crate));
  }

  @Test
  void testImportToExistingCrate() {
    String url = "https://zenodo.org/api/records/6411574";
    var crate = new RoCrate.RoCrateBuilder("name", "description", "2024", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/").build();
    ImportFromZenodo.addZenodoToCrate(url, crate);
    Validator validator = new Validator(new JsonSchemaValidation());
    assertTrue(validator.validate(crate));
  }
}
