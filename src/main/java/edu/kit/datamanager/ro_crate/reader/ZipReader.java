package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.nio.file.Path;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 * A ReaderStrategy implementation which reads from ZipFiles.
 * <p>
 * May be used as a dependency for RoCrateReader. It will unzip
 * the ZipFile in a path relative to the directory this application runs in.
 * By default, it will be `./.tmp/ro-crate-java/zipReader/$UUID/`.
 * <p>
 * NOTE: The resulting crate may refer to these temporary files. Therefore,
 * these files are only being deleted before the JVM exits.
 */
public class ZipReader implements ReaderStrategy {

  protected final String ID = UUID.randomUUID().toString();
  protected Path tempFolder = Path.of(String.format("./.tmp/ro-crate-java/zipReader/%s/", ID));
  protected boolean read = false;

  public ZipReader() {}

  /**
   * Creates a ZipReader which will extract the contents temporary
   * to the given location instead of the default location.
   *
   * @param folderPath            the custom directory to extract
   *                              content to for temporary access.
   * @param shallAddUuidSubfolder if true, the reader will extract
   *                              into subdirectories of the given
   *                              directory. These subdirectories
   *                              will have UUIDs as their names.
   */
  public ZipReader(Path folderPath, boolean shallAddUuidSubfolder) {
    if (shallAddUuidSubfolder) {
      this.tempFolder = folderPath.resolve(ID);
    } else {
      this.tempFolder = folderPath;
    }
  }

  private void readCrate(String location) {
    try {
      File folder = tempFolder.toFile();
      // ensure the directory is clean
      if (folder.isDirectory()) {
        FileUtils.cleanDirectory(folder);
      } else if (folder.isFile()) {
        FileUtils.delete(folder);
      }
      // extract
      try (ZipFile zf = new ZipFile(location)) {
        zf.extractAll(tempFolder.toAbsolutePath().toString());
        this.read = true;
      }
      // register deletion on exit
      FileUtils.forceDeleteOnExit(folder);
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
    File jsonMetadata = tempFolder.resolve("ro-crate-metadata.json").toFile();
    
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
    return tempFolder.toFile();
  }
}
