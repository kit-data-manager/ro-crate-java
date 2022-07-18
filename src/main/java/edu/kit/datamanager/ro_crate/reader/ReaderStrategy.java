package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;

/**
 * Interface for the strategy fo the reader class.
 * This should be implemented if additional strategies are to be build.
 * (e.g., reading from a gzip)
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public interface ReaderStrategy {
  ObjectNode readMetadataJson(String location);

  File readContent(String location);
}
