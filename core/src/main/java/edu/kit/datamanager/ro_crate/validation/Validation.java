package edu.kit.datamanager.ro_crate.validation;

import edu.kit.datamanager.ro_crate.entities.validation.EntityValidationStrategy;
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
    var candidates = ServiceLoader.load(type).iterator();
    if (!candidates.hasNext()) {
      throw new IllegalStateException(
          "No " + type.getSimpleName() + " found on the classpath. Add the "
              + "ro-crate-java-jackson2 or ro-crate-java-jackson3 module as a "
              + "dependency.");
    }
    T discovered = candidates.next();
    if (candidates.hasNext()) {
      throw new IllegalStateException(
          "Multiple " + type.getSimpleName() + " implementations found on the "
              + "classpath; ro-crate-java supports only one JSON backend at a "
              + "time.");
    }
    return discovered;
  }
}
