package edu.kit.datamanager.ro_crate.reader;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Factory for creating common RO-Crate reader instances.
 * Provides convenient static methods to instantiate readers with pre-configured strategies.
 */
public class Readers {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Readers() {}

    /**
     * Creates a reader that reads from ZIP files using input streams.
     *
     * @return A reader configured for ZIP files
     *
     * @see ZipStreamStrategy#ZipStreamStrategy()
     */
    public static CrateReader<InputStream> newZipStreamReader() {
        return new CrateReader<>(new ZipStreamStrategy());
    }

    /**
     * Creates a reader that reads from ZIP files using input streams,
     * extracting to a custom temporary location.
     *
     * @param extractPath Path where ZIP contents should be extracted
     * @param useUuidSubfolder Whether to create a UUID subfolder under extractPath
     * @return A reader configured for ZIP files with custom extraction
     *
     * @see ZipStreamStrategy#ZipStreamStrategy(Path, boolean)
     */
    public static CrateReader<InputStream> newZipStreamReader(Path extractPath, boolean useUuidSubfolder) {
        return new CrateReader<>(new ZipStreamStrategy(extractPath, useUuidSubfolder));
    }

    /**
     * Creates a reader that reads from a folder using a string path.
     *
     * @return A reader configured for folders
     *
     * @see FolderStrategy
     */
    public static CrateReader<String> newFolderReader() {
        return new CrateReader<>(new FolderStrategy());
    }

    /**
     * Creates a reader that reads from a ZIP file using a string path.
     *
     * @return A reader configured for ZIP files
     *
     * @see ZipStrategy#ZipStrategy()
     */
    public static CrateReader<String> newZipPathReader() {
        return new CrateReader<>(new ZipStrategy());
    }

    /**
     * Creates a reader that reads from a ZIP file using a string path,
     * extracting to a custom temporary location.
     *
     * @param extractPath Path where ZIP contents should be extracted
     * @param useUuidSubfolder Whether to create a UUID subfolder under extractPath
     * @return A reader configured for ZIP files with custom extraction
     *
     * @see ZipStrategy#ZipStrategy(Path, boolean)
     */
    public static CrateReader<String> newZipPathReader(Path extractPath, boolean useUuidSubfolder) {
        return new CrateReader<>(new ZipStrategy(extractPath, useUuidSubfolder));
    }
}
