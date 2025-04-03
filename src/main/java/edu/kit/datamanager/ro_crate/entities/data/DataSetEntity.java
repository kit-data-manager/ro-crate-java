package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.kit.datamanager.ro_crate.entities.serializers.HasPartSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashSet;
import java.util.Set;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;

/**
 * A helping class for the creating of Data entities of type Dataset.
 *
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntity extends DataEntity {

    public static final String TYPE = "Dataset";

    @JsonSerialize(using = HasPartSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Set<String> hasPart;

    /**
     * Constructor for instantiating a Dataset entity from the builder.
     *
     * @param entityBuilder the builder passed as argument.
     */
    public DataSetEntity(AbstractDataSetBuilder<?> entityBuilder) {
        super(entityBuilder);
        this.hasPart = entityBuilder.hasPart;
        this.addType(TYPE);
    }

    public void removeFromHasPart(String str) {
        this.hasPart.remove(str);
    }

    @Override
    public void saveToZip(ZipFile zipFile) throws ZipException {
        if (this.getPath() != null) {
            zipFile.addFolder(this.getPath().toFile());
        }
    }

    @Override
    public void saveToStream(ZipOutputStream zipOutputStream) throws ZipException, IOException {
        if (this.getPath() != null) {
            addFolderToZip(zipOutputStream, this.getPath().toAbsolutePath().toString(), this.getPath().getFileName().toString());
        }
    }

    public void addFolderToZip(ZipOutputStream zipOutputStream, String folderPath, String parentPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The provided folder path is not a valid directory: " + folderPath);
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String zipEntryPath = parentPath.isEmpty() ? file.getName() : parentPath + "/" + file.getName();
            if (file.isDirectory()) {
                addFolderToZip(zipOutputStream, file.getAbsolutePath(), zipEntryPath);
            } else {
                addFileToZip(zipOutputStream, file, zipEntryPath);
            }
        }
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, File file, String zipEntryPath) throws IOException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(zipEntryPath);
        zipOutputStream.putNextEntry(zipParameters);

        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, len);
            }
        }

        zipOutputStream.closeEntry();
    }

    public void addToHasPart(String id) {
        this.hasPart.add(id);
    }

    public boolean hasInHasPart(String id) {
        return this.hasPart.contains(id);
    }

    abstract static class AbstractDataSetBuilder<T extends AbstractDataEntityBuilder<T>> extends
            AbstractDataEntityBuilder<T> {

        Set<String> hasPart;

        public AbstractDataSetBuilder() {
            this.hasPart = new HashSet<>();
        }

        public T setHasPart(Set<String> hastPart) {
            this.hasPart = hastPart;
            return self();
        }

        public T addToHasPart(DataEntity dataEntity) {
            if (dataEntity != null) {
                this.hasPart.add(dataEntity.getId());
                this.relatedItems.add(dataEntity.getId());
            }
            return self();
        }

        public T addToHasPart(String dataEntity) {
            if (dataEntity != null) {
                this.hasPart.add(dataEntity);
                this.relatedItems.add(dataEntity);
            }
            return self();
        }

        @Override
        public abstract DataSetEntity build();
    }

    /**
     * Builder for the helping class DataSetEntity.
     */
    public static final class DataSetBuilder extends AbstractDataSetBuilder<DataSetBuilder> {

        @Override
        public DataSetBuilder self() {
            return this;
        }

        @Override
        public DataSetEntity build() {
            return new DataSetEntity(this);
        }
    }
}
