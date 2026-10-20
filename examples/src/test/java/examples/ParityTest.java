package examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Compares the outputs of the example runs: the released 2.1.2 artifact
 * must produce byte-identical metadata to the current code with the
 * jackson2 backend, which again must match the current code with the
 * jackson3 backend. The run tasks (see build.gradle) execute each example
 * once per variant in its own JVM and write to build/examples/&lt;variant&gt;/.
 */
class ParityTest {

  private static final Path EXAMPLES_DIR = Path.of("build", "examples");
  private static final String EXAMPLE = "CreateMinimalCrate";

  private static String output(String variant) throws IOException {
    Path file = EXAMPLES_DIR.resolve(variant).resolve(EXAMPLE + ".txt");
    assertTrue(
      Files.exists(file),
      "missing output " + file + " — the run tasks should have produced it"
    );
    return Files.readString(file);
  }

  @Test
  void legacyMatchesCurrentJackson2() throws IOException {
    assertEquals(output("legacy"), output("current-jackson2"));
  }

  @Test
  void currentJackson2MatchesCurrentJackson3() throws IOException {
    assertEquals(output("current-jackson2"), output("current-jackson3"));
  }
}
