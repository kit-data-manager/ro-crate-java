package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Legacy variant of {@code ValidateCrate}, written against the released
 * 2.1.2 API. The released version has no {@code Validation} entry point, so
 * the JSON-schema validator is instantiated directly; the current code uses
 * the backend-agnostic {@code Validation.crateStrategy()} instead.
 */
public class ValidateCrate {

  static final Path CRATES = Path.of("..", "core", "src", "test", "resources", "crates");

  public static void main(String[] args) throws Exception {
    Validator validator = new Validator(new JsonSchemaValidation());
    StringBuilder report = new StringBuilder();
    for (String fixture : List.of("simple_crate", "extendedContextExample", "hasPartHierarchy")) {
      RoCrate crate = Readers.newFolderReader()
        .readCrate(CRATES.resolve(fixture).toString());
      report
        .append(fixture)
        .append(": ")
        .append(validator.validate(crate))
        .append("\n");
    }
    Files.writeString(Path.of(args[0]), report.toString());
  }
}
