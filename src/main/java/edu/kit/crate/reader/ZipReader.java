package edu.kit.crate.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ZipReader implements IReaderStrategy {

  @Override
  public ObjectNode readMetadataJson(String location) {
    return null;
  }

  @Override
  public File readContent(String location) {
    return null;
  }
}
