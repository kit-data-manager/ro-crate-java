package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.contextual.JsonDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Generic interface for the strategy of the reader class.
 * This allows for flexible input types when implementing different reading strategies.
 *
 * @param <SOURCE_TYPE> the type which determines the source of the crate
 */
public interface GenericReaderStrategy<SOURCE_TYPE> {

    /**
     * Find the JSON metadata file of the crate.
     * <p>
     * Will also find it in case it is in a subfolder, e.g. for ELN style crates.
     *
     * @param location the location to read from
     * @return a file object to the metadata JSON file
     */
    File findMetadataJson(SOURCE_TYPE location) throws IOException;

    /**
     * Read the metadata.json file from the given location.
     *
     * @param location the location to read from
     * @return the parsed metadata JSON file as ObjectNode
     */
    ObjectNode readMetadataJson(SOURCE_TYPE location) throws IOException;

    /**
     * Read the content from the given location.
     *
     * @param location the location to read from
     * @return the content as a File
     */
    File readContent(SOURCE_TYPE location) throws IOException;

    /**
     * Helper method to find the metadata json in a given folder.
     * <p>
     * Considers ELN-style subdirectories.
     *
     * @param root the folder to find the metadata file in
     * @return a file object to the found metadata JSON
     * @throws IOException if no metadata JSON is found
     */
    default File findMetadataJsonByPath(Path root) throws IOException {
        File jsonMetadata = root.resolve(JsonDescriptor.ID).toFile();
        if (jsonMetadata.isFile()) {
            return jsonMetadata;
        }
        // Try to find the metadata file in subdirectories
        File firstSubDir = FileUtils.listFilesAndDirs(
                        root.toFile(),
                        FileFilterUtils.directoryFileFilter(),
                        null
                )
                .stream()
                .limit(50)
                .filter(file -> file.toPath().toAbsolutePath().resolve(JsonDescriptor.ID).toFile().isFile())
                .findFirst()
                .orElseThrow(() -> new FileNotFoundException("No %s found in %s".formatted(
                        JsonDescriptor.ID, root.toString()
                )));
        jsonMetadata = firstSubDir.toPath().resolve(JsonDescriptor.ID).toFile();
        return jsonMetadata;
    }
}