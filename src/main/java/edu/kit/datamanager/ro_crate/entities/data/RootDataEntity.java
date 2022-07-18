package edu.kit.datamanager.ro_crate.entities.data;

import java.io.File;

/**
 * The root DataEntity is basically a DataSet with a default id.
 * This class may be usefully for other functionalities later.
 *
 */
public class RootDataEntity extends DataSetEntity {

  private static final String ID = "./";

  public RootDataEntity(AbstractDataSetBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.setId(ID);
  }

  /**
   * Builder class for easier creation of root data entities.
   */
  public static final class RootDataEntityBuilder
      extends AbstractDataSetBuilder<RootDataEntityBuilder> {

    @Override
    public RootDataEntityBuilder self() {
      return this;
    }

    @Override
    public RootDataEntity build() {
      // small hack not to get the command line message
      this.setSource(new File("./"));
      return new RootDataEntity(this);
    }
  }
}