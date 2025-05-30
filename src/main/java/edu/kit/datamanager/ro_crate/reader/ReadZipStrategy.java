package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.contextual.JsonDescriptor;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.util.FileSystemUtil;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Reads a crate from a ZIP archive (file).
 * <p>
 * This class handles reading and extraction of RO-Crate content from ZIP archives
 * into a temporary directory structure on the file system,
 * which allows accessing the contained files.
 * <p>
 * Supports <a href=https://github.com/TheELNConsortium/TheELNFileFormat>ELN-Style crates</a>,
 * meaning the crate may be either in the zip archive directly or in a single,
 * direct subfolder beneath the root folder (/folder).
 * <p>
 * Note: This implementation checks for up to 50 subdirectories if multiple are present.
 * This is to avoid zip bombs, which may contain a lot of subdirectories,
 * and at the same time gracefully handle valid crated with hidden subdirectories
 * (for example, thumbnails).
 * <p>
 * NOTE: The resulting crate may refer to these temporary files. Therefore,
 * these files are only being deleted before the JVM exits. If you need to free
 * space because your application is long-running or creates a lot of
 * crates, you may use the getters to retrieve information which will help
 * you to clean up manually. Keep in mind that crates may refer to this
 * folder after extraction. Use RoCrateWriter to export it so some
 * persistent location and possibly read it from there, if required. Or use
 * the ZipWriter to write it back to its source.
 */
public class ReadZipStrategy implements GenericReaderStrategy<String> {

  protected final String ID = UUID.randomUUID().toString();
  protected Path temporaryFolder = Path.of(String.format("./.tmp/ro-crate-java/zipReader/%s/", ID));
  protected boolean isExtracted = false;

  /**
   * Crates an instance with the default configuration.
   * <p>
   * The default configuration is to extract the ZipFile to
   * `./.tmp/ro-crate-java/zipReader/$UUID/`.
   */
  public ReadZipStrategy() {}

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
  public ReadZipStrategy(Path folderPath, boolean shallAddUuidSubfolder) {
    if (shallAddUuidSubfolder) {
      this.temporaryFolder = folderPath.resolve(ID);
    } else {
      this.temporaryFolder = folderPath;
    }
  }

  /**
   * @return the identifier which may be used as the name for a subfolder in the temporary directory.
   */
  public String getID() {
    return ID;
  }

  /**
   * @return the folder (considered temporary) where the zipped crate will be or has been extracted to.
   */
  public Path getTemporaryFolder() {
    return temporaryFolder;
  }

  /**
   * @return whether the crate has already been extracted into the temporary folder.
   */
  public boolean isExtracted() {
    return isExtracted;
  }

  private void readCrate(String location) throws IOException {
    File folder = temporaryFolder.toFile();
    FileSystemUtil.mkdirOrDeleteContent(folder);
    // extract
    try (ZipFile zf = new ZipFile(location)) {
      zf.extractAll(temporaryFolder.toAbsolutePath().toString());
      this.isExtracted = true;
    }
    // register deletion on exit
    FileUtils.forceDeleteOnExit(folder);
  }

  @Override
  public ObjectNode readMetadataJson(String location) throws IOException {
    if (!isExtracted) {
      this.readCrate(location);
    }

    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    File jsonMetadata = this.temporaryFolder.resolve(JsonDescriptor.ID).toFile();
    if (!jsonMetadata.isFile()) {
      // Try to find the metadata file in subdirectories
      File firstSubdir = FileUtils.listFilesAndDirs(
            temporaryFolder.toFile(),
            FileFilterUtils.directoryFileFilter(),
            null // not recursive
        )
        .stream()
        .limit(50)
        .filter(file -> file.toPath().toAbsolutePath().resolve(JsonDescriptor.ID).toFile().isFile())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No %s found in zip file".formatted(JsonDescriptor.ID)));
      jsonMetadata = firstSubdir.toPath().resolve(JsonDescriptor.ID).toFile();
    }

    return objectMapper.readTree(jsonMetadata).deepCopy();
  }

  @Override
  public File readContent(String location) throws IOException {
    if (!isExtracted) {
      this.readCrate(location);
    }
    return temporaryFolder.toFile();
  }
}
