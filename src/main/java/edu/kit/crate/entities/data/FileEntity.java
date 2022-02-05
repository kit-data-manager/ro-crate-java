package edu.kit.crate.entities.data;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class FileEntity extends DataEntity {

  private static final String TYPE = "File";

  public FileEntity(AFileEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.addType(TYPE);
  }

  abstract static class AFileEntityBuilder<T extends AFileEntityBuilder<T>> extends
      ADataEntityBuilder<T> {

    public T setEncodingFormat(String encodingFormat) {
      this.addProperty("encodingFormat", encodingFormat);
      return self();
    }

    @Override
    abstract public FileEntity build();
  }

  final static public class FileEntityBuilder extends AFileEntityBuilder<FileEntityBuilder> {

    @Override
    public FileEntityBuilder self() {
      return this;
    }

    @Override
    public FileEntity build() {
      return new FileEntity(this);
    }
  }
}
