package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;

/**
 * Generic interface for the strategy of the reader class.
 * This allows for flexible input types when implementing different reading strategies.
 *
 * @param <T> the type of the location parameter
 */
public interface GenericReaderStrategy<T> {
    /**
     * Read the metadata.json file from the given location.
     *
     * @param location the location to read from
     * @return the parsed metadata.json as ObjectNode
     */
    ObjectNode readMetadataJson(T location);

    /**
     * Read the content from the given location.
     *
     * @param location the location to read from
     * @return the content as a File
     */
    File readContent(T location);
}