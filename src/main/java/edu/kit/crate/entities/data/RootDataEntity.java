package edu.kit.crate.entities.data;

/**
 * The root DataEntity is basically a DataSet with a default id.
 * This class may be usefully for other functionalities later.
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class RootDataEntity extends DataSetEntity {

  private static final String ID = "./";

  public RootDataEntity(ADataSetBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.setId(ID);
  }

  static final public class RootDataEntityBuilder extends ADataSetBuilder<RootDataEntityBuilder> {

    @Override
    public RootDataEntityBuilder self() {
      return this;
    }

    @Override
    public RootDataEntity build() {
      return new RootDataEntity(this);
    }
  }
}