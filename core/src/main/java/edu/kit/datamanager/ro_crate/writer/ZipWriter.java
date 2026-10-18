package edu.kit.datamanager.ro_crate.writer;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Implementation of the writing strategy to provide a way of writing crates to
 * a zip archive.
 *
 * @deprecated Use {@link WriteZipStrategy} instead.
 */
@Deprecated(since = "2.1.0", forRemoval = true)
public class ZipWriter extends WriteZipStrategy {}
