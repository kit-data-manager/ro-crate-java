package edu.kit.crate.entities.contextual;

import edu.kit.crate.entities.AbstractEntity;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class ContextualEntity extends AbstractEntity {

  public ContextualEntity(AContextualEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
  }

  abstract static class AContextualEntityBuilder<T extends AContextualEntityBuilder<T>> extends
      AEntityBuilder<T> {

    @Override
    abstract public ContextualEntity build();
  }

  final static public class ContextualEntityBuilder extends
      AContextualEntityBuilder<ContextualEntityBuilder> {

    @Override
    public ContextualEntityBuilder self() {
      return this;
    }

    @Override
    public ContextualEntity build() {
      return new ContextualEntity(this);
    }
  }
}
