package edu.kit.datamanager.ro_crate.writer;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * A class for writing a crate to a folder.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 *
 * @deprecated Use {@link WriteFolderStrategy} instead.
 */
@Deprecated(since = "2.1.0", forRemoval = true)
public class FolderWriter extends WriteFolderStrategy {}
