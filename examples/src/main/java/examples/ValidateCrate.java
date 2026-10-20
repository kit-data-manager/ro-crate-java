package examples;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.validation.Validation;
import edu.kit.datamanager.ro_crate.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Validates fixture crates with the default schema and prints the result
 * (the boolean only; the human-readable problems go to the error stream and
 * are not part of the comparison). The validator is the one provided by the
 * JSON backend module on the classpath, obtained through the core's
 * {@code Validation} entry point.
 */
public class ValidateCrate {

  static final Path CRATES = Path.of(
    "..",
    "core",
    "src",
    "test",
    "resources",
    "crates"
  );

  public static void main(String[] args) throws Exception {
    Validator validator = new Validator(Validation.crateStrategy());
    StringBuilder report = new StringBuilder();
    for (String fixture : List.of(
      "simple_crate",
      "extendedContextExample",
      "hasPartHierarchy"
    )) {
      RoCrate crate = Readers.newFolderReader().readCrate(
        CRATES.resolve(fixture).toString()
      );
      report
        .append(fixture)
        .append(": ")
        .append(validator.validate(crate))
        .append("\n");
    }
    Files.writeString(Path.of(args[0]), report.toString());
  }
}
