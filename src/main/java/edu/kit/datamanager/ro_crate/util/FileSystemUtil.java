package edu.kit.datamanager.ro_crate.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;

public class FileSystemUtil {
    private FileSystemUtil() {
        // Utility class, no instantiation
    }

    /**
     * Checks if the given ID appears to be a file path.
     * <p>
     * Specifically excludes IDs starting with "doi:", "http", or "https".
     *
     * @param id the ID to check
     * @return true if it looks like a file path, false otherwise
     */
    public static boolean isFilePath(String id) {
        return id != null && !(
                id.startsWith("doi:") ||
                id.startsWith("http://") ||
                id.startsWith("https://")
        );
    }

    /**
     * Gets the parent path of a given path.
     * @param path the path to evaluate.
     * @return the parent path, or null if no parent exists.
     */
    public static String getParentPath(String path) {
        if (path == null || path.equals("./") || path.isEmpty()) {
            return null;
        }

        // Normalize path - remove trailing slash for consistency
        String normalizedPath = path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;

        int lastSlash = normalizedPath.lastIndexOf('/');
        if (lastSlash == -1) {
            return "./"; // Root directory
        }

        String parentPath = normalizedPath.substring(0, lastSlash);

        // If parent is empty, it's root
        if (parentPath.isEmpty()) {
            return "./";
        }

        // For validation, we need to check both with and without trailing slash
        // since files don't have trailing slash but folders do
        return parentPath;
    }

    /**
     * Removes a specific set of given file extensions from a file name, if present.
     * The extensions are case-insensitive. Given "ELN", "eln" or "Eln" will also match.
     * The dot (.) before the extension is also assumed and removed implicitly:
     * <p>
     * Example:
     * filterExtensionsFromFileName("test.eln", Set.of("ELN")) -> "test"
     *
     * @param filename the file name to filter (must not be null)
     * @param extensionsToRemove the extensions to remove (must not be null)
     * @return the filtered file name
     * @throws NullPointerException if any parameter is null
     */
    public static String filterExtensionsFromFileName(String filename, Collection<String> extensionsToRemove) {
        String dot = Matcher.quoteReplacement(".");
        String end = Matcher.quoteReplacement("$");
        for (String extension : extensionsToRemove) {
            // (?i) removes case sensitivity
            filename = filename.replaceFirst("(?i)" + dot + extension + end, "");
        }
        return filename;
    }

    /**
     * Ensures that a given path ends with a trailing slash.
     *
     * @param path the path to check
     * @return the path with a trailing slash if it didn't have one, or the original path
     */
    public static String ensureTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (!path.endsWith("/")) {
            return path + "/";
        }
        return path;
    }

    /**
     * Creates a directory or deletes its content if it already exists.
     *
     * @param folder the folder to create or delete content from
     * @throws IOException if an I/O error occurs
     */
    public static void mkdirOrDeleteContent(File folder) throws IOException {
        File[] files = folder.listFiles();
        boolean isNonEmptyDir = folder.exists()
                && folder.isDirectory()
                && files != null
                && files.length > 0;
        boolean isFile = folder.exists()
                && !folder.isDirectory();

        if (isNonEmptyDir || isFile) {
            FileUtils.forceDelete(folder);
        }
        FileUtils.forceMkdir(folder);
    }
}
