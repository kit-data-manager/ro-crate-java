package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 * Implementation of the reader strategy, providing a way of reading crates from
 * a zip archive.
 */
public class ZipReader implements ReaderStrategy {

  private String uuid = UUID.randomUUID().toString();

  private String tempFolder = "./temp/" + uuid + "/";

  private boolean read = false;

  private void readCrate(String location) {
    try {
      File temp = new File(tempFolder);
      if (temp.exists()) {
        FileUtils.cleanDirectory(new File(tempFolder));
      }
      try (ZipFile zf = new ZipFile(location)) {
        zf.extractAll(tempFolder);
      }
      FileUtils.forceDeleteOnExit(new File(tempFolder));
      this.read = true;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ObjectNode readMetadataJson(String location) {
    if (!read) {
      this.readCrate(location);
    }

    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    File jsonMetadata = new File(tempFolder + "ro-crate-metadata.json");
    
    try {
      return objectMapper.readTree(jsonMetadata).deepCopy();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public File readContent(String location) {
    if (!read) {
      this.readCrate(location);
    }
    return new File(tempFolder);
  }
}
