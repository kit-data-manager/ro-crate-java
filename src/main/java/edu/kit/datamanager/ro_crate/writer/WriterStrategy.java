package edu.kit.datamanager.ro_crate.writer;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Strategy for writing of crates.
 *
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 *
 * @deprecated Use {@link GenericWriterStrategy} instead.
 */
@Deprecated(since = "2.1.0", forRemoval = true)
public interface WriterStrategy extends GenericWriterStrategy<String> {}
