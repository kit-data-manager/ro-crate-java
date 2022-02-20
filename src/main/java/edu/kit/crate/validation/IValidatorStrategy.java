package edu.kit.crate.validation;

import edu.kit.crate.IROCrate;

public interface IValidatorStrategy {
  boolean validate(IROCrate crate);
}
