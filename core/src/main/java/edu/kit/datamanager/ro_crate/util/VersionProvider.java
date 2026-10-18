package edu.kit.datamanager.ro_crate.util;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

public interface VersionProvider {
    /**
     * Returns the version of the ro-crate-java library.
     *
     * @return The version string.
     */
    String getVersion();
}
