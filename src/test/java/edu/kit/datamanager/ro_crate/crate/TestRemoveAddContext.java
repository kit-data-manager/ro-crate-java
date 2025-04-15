package edu.kit.datamanager.ro_crate.crate;

import edu.kit.datamanager.ro_crate.reader.FolderReader;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestRemoveAddContext {
  @Test
  void testAddRemoveValuePair() throws JsonProcessingException {
    RoCrate crate = new RoCrate.RoCrateBuilder().addValuePairToContext("key", "value").build();
    RoCrate defaultCrate = new RoCrate.RoCrateBuilder().build();

    crate.deleteValuePairFromContext("key");

    HelpFunctions.compareTwoCrateJson(crate, defaultCrate);

  }

  @Test
  void testAddRemoveUrl() throws JsonProcessingException {
    RoCrate crate = new RoCrate.RoCrateBuilder().addUrlToContext("https://example.com").build();
    RoCrate defaultCrate = new RoCrate.RoCrateBuilder().build();

    crate.deleteUrlFromContext("https://example.com");

    HelpFunctions.compareTwoCrateJson(crate, defaultCrate);

  }

  @Test
  void testReadDeleteGetContextPair() {
    String crateManifestPath = "/crates/extendedContextExample/";
    crateManifestPath = TestRemoveAddContext.class.getResource(crateManifestPath).getPath();
    RoCrate crate = new RoCrateReader(new FolderReader()).readCrate(crateManifestPath);
    String key = "custom";
    String value = "_:";
    assertEquals(value, crate.readMetadataContextValueOf(key));
    crate.deleteValuePairFromContext(key);
    assertNull(crate.readMetadataContextValueOf(key));
  }

  @Test
  void testReadContextKeys() {
    String crateManifestPath = "/crates/extendedContextExample/";
    crateManifestPath = TestRemoveAddContext.class.getResource(crateManifestPath).getPath();
    RoCrate crate = new RoCrateReader(new FolderReader()).readCrate(crateManifestPath);
    var expected = Set.of("custom", "owl", "datacite", "xsd", "rdfs");
    var given = crate.readMetadataContextKeys();
    for (String key : expected) {
        assertTrue(given.contains(key), "Key " + key + " not found in the context");
    }
  }

  @Test
  void testReadContextPairs() {
    String crateManifestPath = "/crates/extendedContextExample/";
    crateManifestPath = TestRemoveAddContext.class.getResource(crateManifestPath).getPath();
    RoCrate crate = new RoCrateReader(new FolderReader()).readCrate(crateManifestPath);
    var expected = Set.of("custom", "owl", "datacite", "xsd", "rdfs");
    var given = crate.readMetadataContextPairs();
    var keys = given.keySet();
    var values = given.values();
    for (String key : expected) {
        assertTrue(keys.contains(key), "Key " + key + " not found in the context");
        values.forEach(s -> assertFalse(s.isEmpty(), "Value for key " + key + " is empty"));
    }
  }
}
