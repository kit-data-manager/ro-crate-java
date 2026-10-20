package edu.kit.datamanager.ro_crate.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The discovery failures must tell a user who does not know the module layout
 * exactly what to add or exclude, so the messages must name the backend
 * artifacts.
 */
class ValidationDiscoveryTest {

  @Test
  void missingBackendMessageNamesTheBackendArtifacts() {
    var exception = assertThrows(
      IllegalStateException.class,
      () -> Validation.uniqueCandidate(
        Collections.<ValidatorStrategy>emptyIterator(),
        ValidatorStrategy.class
      )
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java"),
      "message should name the Jackson 2 artifact: " + exception.getMessage()
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java-jackson3"),
      "message should name the Jackson 3 artifact: " + exception.getMessage()
    );
  }

  @Test
  void multipleBackendsMessageSuggestsTheFix() {
    ValidatorStrategy strategy = crate -> true;
    var exception = assertThrows(
      IllegalStateException.class,
      () -> Validation.uniqueCandidate(
        List.<ValidatorStrategy>of(strategy, strategy).iterator(),
        ValidatorStrategy.class
      )
    );
    assertTrue(
      exception.getMessage().contains("exclude"),
      "message should suggest excluding one backend: " + exception.getMessage()
    );
    assertTrue(
      exception.getMessage().contains("edu.kit.datamanager:ro-crate-java-jackson3"),
      "message should name the Jackson 3 artifact: " + exception.getMessage()
    );
  }
}
