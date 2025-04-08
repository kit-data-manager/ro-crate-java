package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import static edu.kit.datamanager.ro_crate.special.IdentifierUtils.isUrl;
import edu.kit.datamanager.ro_crate.util.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
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

    /**
     * Adds an author ID to the entity.
     *
     * Calling this multiple times will add multiple author IDs.
     *
     * @param id the identifier of the author.
     */
    public void addAuthorId(String id) {
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
     * when the crate is being written to a zip archive.
     *
     * @param zipStream The zip output stream where it should be written.
     * @throws ZipException when something goes wrong with the writing to the
     * zip file.
     * @throws IOException If opening the file input stream fails.
     */
    public void saveToStream(ZipOutputStream zipStream) throws ZipException, IOException {
        if (this.path != null) {
            ZipUtil.addFileToZipStream(zipStream, this.path.toFile(), this.getId());
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
        if (this.getPath() != null) {
            if (this.getPath().toFile().isDirectory()) {
                FileUtils.copyDirectory(this.getPath().toFile(), file.toPath().resolve(this.getId()).toFile());
            } else {
                FileUtils.copyFile(this.getPath().toFile(), file.toPath().resolve(this.getId()).toFile());
            }
        }
    }

    @JsonIgnore
    public Path getPath() {
        return path;
    }

    abstract static class AbstractDataEntityBuilder<T extends AbstractDataEntityBuilder<T>> extends
            AbstractEntityBuilder<T> {

        private Path location;
        private List<String> authors = new ArrayList<>();

        /**
         * Sets the location of the data entity.
         *
         * If the ID has not been set manually in beforehand, it will be derived
         * from the path. Use {@link #setId(String)} to override it or set it in
         * beforehand. Note that another call of {@link #setLocation(Path)} will
         * not override the ID as it has been set by the previous call!
         *
         * @param path the location of the data. May be null, in which case
         * nothing happens.
         * @return this builder
         */
        public T setLocation(Path path) {
            if (path != null) {
                if (this.getId() == null) {
                    this.setId(path.getFileName().toString());
                }
                this.location = path;
            }
            return self();
        }

        /**
         * A variant of {@link #setLocation(Path)} which may throw exceptions.
         *
         * @param path the location of the data
         * @return this builder
         * @throws IllegalArgumentException if path is null
         */
        public T setLocationWithExceptions(Path path) throws IllegalArgumentException {
            if (path == null) {
                throw new IllegalArgumentException("The given path should not be null.");
            }
            return setLocation(path);
        }

        /**
         * Same as {@link #setLocation(Path)} but instead of associating this
         * entity with a file, it will point to some place on the internet.
         *
         * Via the specification, this means the uri will be set as the ID. This
         * call is therefore equivalent to {@link #setId(String)}.
         *
         * @param uri the URI, should point at the data reachable on the
         * internet.
         * @return this builder
         */
        public T setLocation(URI uri) {
            if (uri != null && this.getId() == null) {
                this.setId(uri.toString());
            }
            return self();
        }

        /**
         * A variant of {@link #setLocation(URI)} which may throw exceptions.
         *
         * @param uri the given uri
         * @return this builder
         * @throws IllegalArgumentException if uri is null or not a valid URL
         */
        public T setLocationWithExceptions(URI uri) throws IllegalArgumentException {
            if (!isUrl(uri.toString())) {
                throw new IllegalArgumentException("This Data Entity remote ID does not resolve to a valid URL.");
            }
            return setLocation(uri);
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
     *
     * If not explicitly mentioned, all methods avoid Exceptions and will
     * silently ignore null-parameters, in which case nothing will happen. Use
     * the available *WithExceptions-methods in case you need them.
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
