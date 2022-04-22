package edu.kit.crate.validation;

import edu.kit.crate.Crate;

/**
 * Interface for the validation strategy.
 */
public interface ValidatorStrategy {
  boolean validate(Crate crate);
}
