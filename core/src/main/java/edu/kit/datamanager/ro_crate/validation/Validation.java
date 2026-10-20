package edu.kit.datamanager.ro_crate.validation;

import edu.kit.datamanager.ro_crate.entities.validation.EntityValidationStrategy;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Access point for the JSON-schema validation strategies.
 *
 * <p>The concrete implementations live in the JSON backend modules
 * (ro-crate-java-jackson2 or ro-crate-java-jackson3) and are discovered via
 * {@link ServiceLoader}; exactly one backend must be on the classpath. This
 * keeps the core free of any concrete JSON implementation.
 */
public final class Validation {

  private static volatile ValidatorStrategy crateStrategy;
  private static volatile EntityValidationStrategy entityStrategy;

  private Validation() {}

  /**
   * @return the crate-level JSON-schema validation of the active JSON backend
   */
  public static ValidatorStrategy crateStrategy() {
    ValidatorStrategy result = crateStrategy;
    if (result == null) {
      result = discover(ValidatorStrategy.class);
      crateStrategy = result;
    }
    return result;
  }

  /**
   * @return the entity-level JSON-schema validation of the active JSON backend
   */
  public static EntityValidationStrategy entityStrategy() {
    EntityValidationStrategy result = entityStrategy;
    if (result == null) {
      result = discover(EntityValidationStrategy.class);
      entityStrategy = result;
    }
    return result;
  }

  private static <T> T discover(Class<T> type) {
    return uniqueCandidate(ServiceLoader.load(type).iterator(), type);
  }

  /**
   * Returns the single candidate or throws an exception that names the
   * backend artifacts to add or exclude, so users who do not know the module
   * layout can fix the classpath themselves.
   */
  static <T> T uniqueCandidate(Iterator<T> candidates, Class<T> type) {
    if (!candidates.hasNext()) {
      throw new IllegalStateException(
        "No " +
          type.getSimpleName() +
          " found on the classpath. Add a JSON " +
          "backend as a dependency: edu.kit.datamanager:ro-crate-java " +
          "(Jackson 2 backend) or edu.kit.datamanager:ro-crate-java-jackson3 " +
          "(Jackson 3 backend)."
      );
    }
    T discovered = candidates.next();
    if (candidates.hasNext()) {
      throw new IllegalStateException(
        "Multiple " +
          type.getSimpleName() +
          " implementations found on the " +
          "classpath; ro-crate-java supports only one JSON backend at a " +
          "time. Keep the backend matching your Jackson version and exclude " +
          "the other one, e.g. exclude edu.kit.datamanager:ro-crate-java " +
          "(Jackson 2 backend) and add " +
          "edu.kit.datamanager:ro-crate-java-jackson3 (Jackson 3 backend)."
      );
    }
    return discovered;
  }
}
