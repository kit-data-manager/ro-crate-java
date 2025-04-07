package edu.kit.datamanager.ro_crate.preview.model;

import java.util.List;

/**
 *
 * @author jejkal
 */
public class ROCratePreviewModel {

    public ROCrate crate;
    public List<Dataset> datasets;
    public List<File> files;

    public ROCrate getCrate() {
        return crate;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public List<File> getFiles() {
        return files;
    }

    public static class ROCrate {

        public String name;
        public String description;
        public String type;
        public String license;
        public String datePublished;
        public List<Part> hasPart;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String getLicense() {
            return license;
        }

        public String getDatePublished() {
            return datePublished;
        }

        public List<Part> getHasPart() {
            return hasPart;
        }

    }

    public static class Part {

        public String id;
        public String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

    }

    public static class Dataset {

        public String id;
        public String name;
        public String description;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    public static class File {

        public String id;
        public String name;
        public String description;
        public String contentSize;
        public String encodingFormat;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getContentSize() {
            return contentSize;
        }

        public String getEncodingFormat() {
            return encodingFormat;
        }

    }
}
