package examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Compares the outputs of the example runs. Each example is executed once per
 * variant (see the {@code variants} map in build.gradle; each run happens in
 * its own JVM) and writes {@code build/examples/<variant>/<Example>.txt}.
 *
 * <p>This test asserts that consecutive variants produce byte-identical
 * output: the released 2.1.2 artifact ({@code legacy}) matches the current
 * code with the jackson2 backend, which matches the current code with the
 * jackson3 backend.
 *
 * <p>New examples are covered automatically: the example list is derived from
 * the output files, and the variant names are passed in from the build via
 * the {@code rocrate.parity.variants} system property (single source of
 * truth: the {@code variants} map in build.gradle).
 */
class ParityTest {

  private static final Path EXAMPLES_DIR = Path.of("build", "examples");
  private static final String VARIANTS_PROPERTY = "rocrate.parity.variants";
  private static final String OUTPUT_SUFFIX = ".txt";

  private static List<String> variants() {
    String value = System.getProperty(VARIANTS_PROPERTY);
    assertTrue(
      value != null,
      "missing system property " +
        VARIANTS_PROPERTY +
        " — run the tests through the Gradle build"
    );
    return List.of(value.split(","));
  }

  /**
   * The example names as found in the output of the first variant; also
   * asserts that every variant produced the same set of examples.
   */
  private static List<String> examples() throws IOException {
    List<String> expected = exampleNames(variants().get(0));
    for (String variant : variants()) {
      assertEquals(
        expected,
        exampleNames(variant),
        "variant '" + variant + "' produced a different set of examples"
      );
    }
    return expected;
  }

  private static List<String> exampleNames(String variant) throws IOException {
    Path dir = EXAMPLES_DIR.resolve(variant);
    assertTrue(
      Files.isDirectory(dir),
      "missing output directory " +
        dir +
        " — the run tasks should have produced it"
    );
    try (Stream<Path> files = Files.list(dir)) {
      return files
        .map(file -> file.getFileName().toString())
        .filter(name -> name.endsWith(OUTPUT_SUFFIX))
        .map(name -> name.substring(0, name.length() - OUTPUT_SUFFIX.length()))
        .sorted()
        .toList();
    }
  }

  private static String output(String variant, String example)
    throws IOException {
    Path file = EXAMPLES_DIR.resolve(variant).resolve(example + OUTPUT_SUFFIX);
    assertTrue(
      Files.exists(file),
      "missing output " + file + " — the run tasks should have produced it"
    );
    return Files.readString(file);
  }

  @Test
  void everyVariantProducedTheSameExamples() throws IOException {
    examples();
  }

  @Test
  void consecutiveVariantsProduceIdenticalOutput() throws IOException {
    List<String> variants = variants();
    for (String example : examples()) {
      for (int i = 0; i < variants.size() - 1; i++) {
        String previous = variants.get(i);
        String next = variants.get(i + 1);
        assertEquals(
          output(previous, example),
          output(next, example),
          "output of '" +
            example +
            "' differs between '" +
            previous +
            "' and '" +
            next +
            "'"
        );
      }
    }
  }
}
