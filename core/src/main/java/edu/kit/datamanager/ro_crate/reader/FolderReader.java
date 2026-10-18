package edu.kit.datamanager.ro_crate.reader;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * A class for reading a crate from a folder.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 *
 * @deprecated Use {@link ReadFolderStrategy} instead.
 */
@Deprecated(since = "2.1.0", forRemoval = true)
public class FolderReader extends ReadFolderStrategy {}
