package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.UUID;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.apache.commons.io.FileUtils;

/**
 * A ZIP file reader implementation of the StreamReaderStrategy interface.
 * This class handles reading and extraction of RO-Crate content from ZIP archives
 * into a temporary directory structure, which allows for accessing the contained files.
 *
 * @author jejkal
 */
public class ZipStreamReader implements StreamReaderStrategy {

    protected final String ID = UUID.randomUUID().toString();
    protected Path temporaryFolder = Path.of(String.format("./.tmp/ro-crate-java/zipStreamReader/%s/", ID));
    protected boolean isExtracted = false;

    /**
     * Crates a ZipStreamReader with the default configuration as described in
     * the class documentation.
     */
    public ZipStreamReader() {
    }

    /**
     * Creates a ZipStreamReader which will extract the contents temporary to
     * the given location instead of the default location.
     *
     * @param folderPath the custom directory to extract content to for
     * temporary access.
     * @param shallAddUuidSubfolder if true, the reader will extract into
     * subdirectories of the given directory. These subdirectories will have
     * UUIDs as their names.
     */
    public ZipStreamReader(Path folderPath, boolean shallAddUuidSubfolder) {
        if (shallAddUuidSubfolder) {
            this.temporaryFolder = folderPath.resolve(ID);
        } else {
            this.temporaryFolder = folderPath;
        }
    }

    /**
     * @return the identifier which may be used as the name for a subfolder in
     * the temporary directory.
     */
    public String getID() {
        return ID;
    }

    /**
     * @return the folder (considered temporary) where the zipped crate will be
     * or has been extracted to.
     */
    public Path getTemporaryFolder() {
        return temporaryFolder;
    }

    /**
     * @return whether the crate has already been extracted into the temporary
     * folder.
     */
    public boolean isExtracted() {
        return isExtracted;
    }

    /**Read the create metadata and content from the provided input stream.
     * 
     * @param stream The input stream.
     */
    private void readCrate(InputStream stream) {
        try {
            File folder = temporaryFolder.toFile();
            // ensure the directory is clean
            if (folder.exists()) {
                if (folder.isDirectory()) {
                    FileUtils.cleanDirectory(folder);
                } else if (folder.isFile()) {
                    FileUtils.delete(folder);
                }
            } else {
                FileUtils.forceMkdir(folder);
            }

            LocalFileHeader localFileHeader;
            int readLen;
            byte[] readBuffer = new byte[4096];

            try (ZipInputStream zipInputStream = new ZipInputStream(stream)) {
                while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                    File extractedFile = new File(folder, localFileHeader.getFileName());
                    try (OutputStream outputStream = new FileOutputStream(extractedFile)) {
                        while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                            outputStream.write(readBuffer, 0, readLen);
                        }
                    }
                }
            }
            this.isExtracted = true;
            // register deletion on exit
            FileUtils.forceDeleteOnExit(folder);
        } catch (IOException ex) {
            logger.error("Failed to read crate from input stream.", ex);
        }
    }

    @Override
    public ObjectNode readMetadataJson(InputStream stream) {
        if (!isExtracted) {
            this.readCrate(stream);
        }

        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        File jsonMetadata = temporaryFolder.resolve("ro-crate-metadata.json").toFile();

        try {
            return objectMapper.readTree(jsonMetadata).deepCopy();
        } catch (IOException e) {
            logger.error("Failed to deserialize crate metadata.", e);
            return null;
        }
    }

    @Override
    public File readContent(InputStream stream) {
        if (!isExtracted) {
            this.readCrate(stream);
        }
        return temporaryFolder.toFile();
    }
}
