package edu.kit.datamanager.ro_crate.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {

  RoCrateMetadataContext context;

  @BeforeEach
  void initContext() {
    // this will load the default context
    this.context = new RoCrateMetadataContext();
  }

  @Test
  void testContext() {

    var entity = this.context.getContextJsonEntity();

    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    // we expect the default RO-crate context;
    var expected = objectMapper.createObjectNode();
    expected.put("@context", "https://w3id.org/ro/crate/1.1/context");
    HelpFunctions.compare(entity, expected, true);

    // this entity is not correct
    // there is no type blabla in schema.org
    var contextualEntity = new ContextualEntity.ContextualEntityBuilder()
        .setId("dkfaj")
        .addType("blabla")
        .build();

    assertFalse(this.context.checkEntity(contextualEntity));

    // this entity is correct the type Accommodation exists in schema.org,
    // so it should be valid
    var contextualEntityCorrect = new ContextualEntity.ContextualEntityBuilder()
        .setId("dkfaj")
        .addType("Accommodation")
        .addProperty("name", "ffjkjk")
        .build();

    assertTrue(this.context.checkEntity(contextualEntityCorrect));

    // this entity has correct types but wrong field
    var contextualEntityWrongField = new ContextualEntity.ContextualEntityBuilder()
        .setId("dkfaj")
        .addType("Accommodation")
        .addProperty("notExisting", "kfdjfk")
        .build();

    assertFalse(this.context.checkEntity(contextualEntityWrongField));
  }

  @Test
  void creationUrlTest() {
    RoCrateMetadataContext newContext = new RoCrateMetadataContext(
        List.of("www.example.com", "www.example.com/context"));

    var contextualEntityWrongField = new ContextualEntity.ContextualEntityBuilder()
        .setId("dkfaj")
        .addType("Accommodation")
        .build();
    // this will be false since here there is no default context, and the example
    // context could not be retrieved
    assertFalse(newContext.checkEntity(contextualEntityWrongField));
    // the two example context should be nevertheless added to the final result
    var jsonNode = newContext.getContextJsonEntity();
    assertEquals(2, jsonNode.get("@context").size());
  }

  @Test
  void creationFromPairsJsonTest() {
    var objectMapper = MyObjectMapper.getMapper();

    ObjectNode rawContext = objectMapper.createObjectNode();
    rawContext.put("house", "www.example.con/house");
    rawContext.put("road", "www.example.con/road");

    ObjectNode rawCrate = objectMapper.createObjectNode();
    rawCrate.set("@context", rawContext);
    RoCrateMetadataContext newContext = new RoCrateMetadataContext(rawContext);
    assertNotNull(newContext);

    HelpFunctions.compare(newContext.getContextJsonEntity(), rawCrate, true);
  }

  @Test
  void deletePairTest() {
    RoCrateMetadataContext context = new RoCrateMetadataContext();
    RoCrateMetadataContext emptyContext = new RoCrateMetadataContext();

    context.addToContext("key", "value");

    context.deleteValuePairFromContext("key");

    HelpFunctions.compare(context.getContextJsonEntity(), emptyContext.getContextJsonEntity(), true);
  }

  @Test
  void deleteNonExistingPairTest() {
    RoCrateMetadataContext context = new RoCrateMetadataContext();
    RoCrateMetadataContext emptyContext = new RoCrateMetadataContext();

    context.deleteValuePairFromContext("key");

    HelpFunctions.compare(context.getContextJsonEntity(), emptyContext.getContextJsonEntity(), true);
  }

  @Test
  void deleteUrlTest() {
    RoCrateMetadataContext context = new RoCrateMetadataContext();
    RoCrateMetadataContext emptyContext = new RoCrateMetadataContext();

    context.addToContextFromUrl("www.example.com");

    context.deleteUrlFromContext("www.example.com");

    HelpFunctions.compare(context.getContextJsonEntity(), emptyContext.getContextJsonEntity(), true);

  }

  @Test
  void deleteNonExistentUrlTest() {
    RoCrateMetadataContext context = new RoCrateMetadataContext();
    RoCrateMetadataContext emptyContext = new RoCrateMetadataContext();

    context.deleteUrlFromContext("www.example.com");

    HelpFunctions.compare(context.getContextJsonEntity(), emptyContext.getContextJsonEntity(), true);
  }

  @Test
  void creationFromUrlAndPairsTest() {

    var objectMapper = MyObjectMapper.getMapper();

    ObjectNode res = objectMapper.createObjectNode();
    ArrayNode arr = objectMapper.createArrayNode();

    ObjectNode node = objectMapper.createObjectNode();
    node.put("house", "www.example.con/house");
    node.put("road", "www.example.con/road");
    arr.add(node);
    arr.add("www.example.com/context");

    res.set("@context", arr);
    RoCrateMetadataContext newContext = new RoCrateMetadataContext(arr);
    HelpFunctions.compare(newContext.getContextJsonEntity(), res, true);

    var data = new DataEntity.DataEntityBuilder()
        .addType("house")
        .setId("https://www.example.com/entity")
        .build();
    // house is in the context
    assertTrue(newContext.checkEntity(data));
  }
}
