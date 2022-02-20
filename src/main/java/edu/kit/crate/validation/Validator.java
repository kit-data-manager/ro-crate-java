package edu.kit.crate.validation;

import edu.kit.crate.IROCrate;

public class Validator {

  private IValidatorStrategy strategy;

  public Validator(IValidatorStrategy strategy) {
    this.strategy = strategy;
  }

  public boolean validate(IROCrate crate) {
    return this.strategy.validate(crate);
  }
}
