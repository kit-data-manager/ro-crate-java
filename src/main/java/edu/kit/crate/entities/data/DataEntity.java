package edu.kit.crate.entities.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * The base class of every data entity.
 *
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class DataEntity extends AbstractEntity {

  @JsonIgnore
  private File source;

  /**
   * The constructor that takes an DataEntity builder and instantiates the entity.
   *
   * @param entityBuilder the builder passed as argument.
   */
  public DataEntity(AbstractDataEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    if (!entityBuilder.authors.isEmpty()) {
      this.addIdListProperties("author", entityBuilder.authors);
    }
    this.source = entityBuilder.location;
    if (this.getSource() == null) {
      UrlValidator urlValidator = new UrlValidator();
      if (!urlValidator.isValid(URLDecoder.decode(this.getId(), StandardCharsets.UTF_8))) {
        System.out.println("This Data Entity remote ID does not resolve to a valid URL.");
      }
    }
  }

  public void setAuthor(String id) {
    this.addIdProperty("author", id);
  }

  /**
   * If the data entity contains a physical file.
   * This method will write it when the crate is being written to a zip archive.
   *
   * @param zipFile the zipFile where it should be written.
   * @throws ZipException when something goes wrong with the writing to the zip file.
   */
  public void saveToZip(ZipFile zipFile) throws ZipException {
    if (this.source != null) {
      ZipParameters zipParameters = new ZipParameters();
      zipParameters.setFileNameInZip(this.getId());
      zipFile.addFile(this.source, zipParameters);
    }
  }

  /**
   * If the data entity contains a physical file.
   * This method will write it when the crate is being written to a folder.
   *
   * @param file the folder location where the entity should be written.
   * @throws IOException if something goes wrong with the writing.
   */
  public void savetoFile(File file) throws IOException {
    if (this.getSource() != null) {
      if (this.getSource().isDirectory()) {
        FileUtils.copyDirectory(this.getSource(), file.toPath().resolve(this.getId()).toFile());
      } else {
        FileUtils.copyFile(this.getSource(), file.toPath().resolve(this.getId()).toFile());
      }
    }
  }

  public File getSource() {
    return source;
  }

  public void setSource(File source) {
    this.source = source;
  }

  abstract static class AbstractDataEntityBuilder<T extends AbstractDataEntityBuilder<T>> extends
      AbstractEntityBuilder<T> {

    File location;
    List<String> authors = new ArrayList<>();

    public T setSource(File file) {
      if (file != null) {
        if (this.getId() == null) {
          this.setId(URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        }
        this.location = file;
      }
      return self();
    }

    public T setLicense(String id) {
      this.addIdProperty("license", id);
      return self();
    }

    public T setLicense(ContextualEntity license) {
      this.addIdProperty("license", license.getId());
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
    public abstract DataEntity build();
  }

  /**
   * Data Entity builder class that allows for easier data entity creation.
   */
  public static final class DataEntityBuilder extends AbstractDataEntityBuilder<DataEntityBuilder> {

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