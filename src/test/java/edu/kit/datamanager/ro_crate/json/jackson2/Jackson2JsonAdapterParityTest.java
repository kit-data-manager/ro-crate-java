package edu.kit.datamanager.ro_crate.json.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Parity tests: the Jackson 2 adapter must behave like the ObjectMapper the
 * library used before the conversion (former {@code MyObjectMapper}),
 * byte-for-byte on output and feature-for-feature on parsing.
 */
class Jackson2JsonAdapterParityTest {

  private static final ObjectMapper LEGACY_MAPPER = new ObjectMapper()
    .enable(
      com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
    )
    .enable(
      com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS
    );

  @Test
  void parseMatchesLegacyMapperTree() throws IOException {
    String json =
      "{\"name\":\"crate\",\"items\":[\"one\"],\"nested\":{\"a\":1}}";
    assertEquals(
      Jackson2JsonAdapter.toNeutral(LEGACY_MAPPER.readTree(json)),
      Json.parse(json)
    );
  }

  @Test
  void writeMatchesLegacyMapperOutput() throws IOException {
    JsonObject object = JsonObject.create()
      .put("z", "last")
      .put("a", "first")
      .put("n", 1)
      .put("d", 2.5)
      .put("b", true)
      .putNull("nil")
      .set("items", JsonArray.create().add("x").add("y"));
    assertEquals(
      LEGACY_MAPPER.writeValueAsString(Jackson2JsonAdapter.toJackson(object)),
      Json.write(object)
    );
  }

  @Test
  void readTreeBehaviorIsIdenticalToLegacyMapper() throws IOException {
    // readTree does NOT unwrap single-element arrays even with the legacy
    // features enabled (verified against Jackson 2.22.1) — the adapter must
    // behave exactly the same
    String json = "{\"hasPart\": [{\"@id\": \"a\"}],\"tags\": \"one\"}";
    assertEquals(
      Jackson2JsonAdapter.toNeutral(LEGACY_MAPPER.readTree(json)),
      Json.parse(json)
    );
    assertTrue(
      ((JsonObject) Json.parse(json)).get("hasPart").isArray(),
      "readTree keeps single-element arrays as arrays"
    );
  }

  @Test
  void roundTripThroughAdapterIsStable() throws IOException {
    String json =
      "{\"@context\":\"https://w3id.org/ro/crate/1.1/context\"," +
      "\"@graph\":[{\"@id\":\"./\",\"name\":\"m\\\"in\",\"num\":1.0," +
      "\"unicode\":\"äöü €\"}]}";
    assertEquals(json, Json.write(Json.parse(json)));
  }

  @Test
  void adapterIsDiscoverableViaServiceLoader() {
    assertEquals(Jackson2JsonAdapter.class, Json.adapter().getClass());
  }
}
