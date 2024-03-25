package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import static edu.kit.datamanager.ro_crate.special.UriUtil.isUrl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

/**
 * The base class of every data entity.
 *
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class DataEntity extends AbstractEntity {

    @JsonIgnore
    private Path path;

    /**
     * The constructor that takes an DataEntity builder and instantiates the
     * entity.
     *
     * @param entityBuilder the builder passed as argument.
     */
    public DataEntity(AbstractDataEntityBuilder<?> entityBuilder) {
        super(entityBuilder);
        if (!entityBuilder.authors.isEmpty()) {
            this.addIdListProperties("author", entityBuilder.authors);
        }
        this.path = entityBuilder.location;
    }

    public void setAuthor(String id) {
        this.addIdProperty("author", id);
    }

    /**
     * If the data entity contains a physical file. This method will write it
     * when the crate is being written to a zip archive.
     *
     * @param zipFile the zipFile where it should be written.
     * @throws ZipException when something goes wrong with the writing to the
     * zip file.
     */
    public void saveToZip(ZipFile zipFile) throws ZipException {
        if (this.path != null) {
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setFileNameInZip(this.getId());
            zipFile.addFile(this.path.toFile(), zipParameters);
        }
    }

    /**
     * If the data entity contains a physical file. This method will write it
     * when the crate is being written to a folder.
     *
     * @param file the folder location where the entity should be written.
     * @throws IOException if something goes wrong with the writing.
     */
    public void savetoFile(File file) throws IOException {
        if (this.getContent() != null) {
            if (this.getContent().toFile().isDirectory()) {
                FileUtils.copyDirectory(this.getContent().toFile(), file.toPath().resolve(this.getId()).toFile());
            } else {
                FileUtils.copyFile(this.getContent().toFile(), file.toPath().resolve(this.getId()).toFile());
            }
        }
    }

    @JsonIgnore
    public Path getContent() {
        return path;
    }

    abstract static class AbstractDataEntityBuilder<T extends AbstractDataEntityBuilder<T>> extends
            AbstractEntityBuilder<T> {

        Path location;

        List<String> authors = new ArrayList<>();

        /**
         * adding a content to the data entity.
         *
         * @param path the path of the content
         * @param id the identifier of the content
         * @return
         */
        public T addContent(Path path, String id) {
            if (path != null && id != null) {
                this.addId(id);
                this.location = path;
            } else {
                throw new IllegalArgumentException("The given path and Identifier should not be null.");
            }
            return self();
        }

        /**
         * adding a web content to the data entity. If the given uri is not a
         * url, the content will not be added.
         *
         * @param uri the given uri
         * @return
         */
        public T addContent(URI uri) {
            if (isUrl(uri.toString())) {
                this.addId(uri.toString());
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
