package edu.kit.datamanager.ro_crate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ClasspathPropertiesVersionProvider implements VersionProvider {
    private static final String PROPERTIES_FILE = "ro-crate-java.properties";
    private static final String VERSION_KEY = "version";

    /**
     * Cached version to avoid repeated file/resource reads.
     */
    private String cachedVersion = null;

    /**
     * Constructs a ClasspathPropertiesVersionProvider that reads the version from a properties file in the classpath.
     */
    public ClasspathPropertiesVersionProvider() {
        this.cachedVersion = getVersion();
    }

    @Override
    public String getVersion() {
        if (cachedVersion != null) {
            return cachedVersion;
        }

        URL resource = this.getClass().getResource("/version.properties");
        if (resource == null) {
            throw new IllegalStateException(
                    "version.properties not found in classpath. This indicates a build configuration issue.");
        }

        try (InputStream input = resource.openStream()) {
            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty("version");
            if (version == null || version.trim().isEmpty()) {
                throw new IllegalStateException("No version property found in version.properties");
            }
            return version.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read version from properties file", e);
        }
    }
}
