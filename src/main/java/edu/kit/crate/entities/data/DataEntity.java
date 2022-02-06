package edu.kit.crate.entities.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class DataEntity extends AbstractEntity {

  @JsonIgnore
  private File location;

  public DataEntity(ADataEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.addIdListProperties("author", entityBuilder.authors);
    this.location = entityBuilder.location;
  }

  public void setAuthor(String id) {
    this.addIdProperty("author", id);
  }

  public void saveToZip(ZipFile zipFile) throws ZipException {
    if (this.location != null) {
      zipFile.addFile(this.location);
    }
  }

  public void savetoFile(File file) throws IOException {
    if (this.getLocation() != null) {
      if (this.getLocation().isDirectory()) {
        FileUtils.copyDirectoryToDirectory(this.getLocation(), file);
      } else {
        FileUtils.copyFileToDirectory(this.getLocation(), file);
      }
    }
  }

  public File getLocation() {
    return location;
  }

  public void setLocation(File location) {
    this.location = location;
  }

  abstract static class ADataEntityBuilder<T extends ADataEntityBuilder<T>> extends
      AEntityBuilder<T> {

    File location;
    List<String> authors = new ArrayList<>();

    public T setLocation(File file) {
      this.location = file;
      return self();
    }

    public T setLicense(String id) {
      this.addIdProperty("license", id);
      return self();
    }

    public T setLicense(ContextualEntity license) {
      this.addIdProperty("licence", license.getId());
      return self();
    }

    public T addAuthor(String id) {
      this.authors.add(id);
      return self();
    }

    public T setContentLocation(String id) {
      this.addIdProperty("contentLocation", id);
      return self();
    }

    @Override
    abstract public DataEntity build();
  }

  final static public class DataEntityBuilder extends ADataEntityBuilder<DataEntityBuilder> {

    @Override
    public DataEntityBuilder self() {
      return this;
    }

    @Override
    public DataEntity build() {
      return new DataEntity(this);
    }
  }
}