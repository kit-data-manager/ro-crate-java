package edu.kit.rocrate.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.context.ROCrateMetadataContext;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {

  ROCrateMetadataContext context;

  @BeforeEach
  void initContext() {
    // this will load the default context
    this.context = new ROCrateMetadataContext();
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
    ROCrateMetadataContext newContext = new ROCrateMetadataContext(List.of("www.example.com", "www.example.com/context"));

    var contextualEntityWrongField = new ContextualEntity.ContextualEntityBuilder()
        .setId("dkfaj")
        .addType("Accommodation")
        .build();
    // this will be false since here there is no default context, and the example context could not be retrieved
    assertFalse(newContext.checkEntity(contextualEntityWrongField));
    // the two example context should be nevertheless added to the final result
    var jsonNode = newContext.getContextJsonEntity();
    assertEquals(jsonNode.get("@context").size(), 2);
  }

  @Test
  void creationFromPairsJsonTest() {
    var objectMapper = MyObjectMapper.getMapper();

    ObjectNode res = objectMapper.createObjectNode();
    ObjectNode node = objectMapper.createObjectNode();
    node.put("house", "www.example.con/house");
    node.put("road", "www.example.con/road");

    res.set("@context", node);
    ROCrateMetadataContext newContext = new ROCrateMetadataContext(node);
    HelpFunctions.compare(newContext.getContextJsonEntity(), res, true);
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
    ROCrateMetadataContext newContext = new ROCrateMetadataContext(arr);
    HelpFunctions.compare(newContext.getContextJsonEntity(), res, true);

    var data = new DataEntity.DataEntityBuilder()
        .addType("house")
        .setId("https://www.example.com/entity")
        .build();
    // house is in the context
    assertTrue(newContext.checkEntity(data));
  }
}
