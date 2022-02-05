package edu.kit.rocrate.entities.contextual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class OrganizationEntityTest {

  @Test
  void testSerialization() throws IOException {
    OrganizationEntity organization = new OrganizationEntity.OrganizationEntityBuilder()
        .setId("https://ror.org/03f0f6041")
        .setAddress("set")
        .setEmail("Sydney@sy.kit")
        .setTelephone("0665445")
        .setLocationId("#djfffff")
        .addProperty("url", "https://ror.org/03f0f6041")
        .addProperty("name", "University of Technology Sydney")
        .build();

    InputStream inputStream =
        OrganizationEntityTest.class.getResourceAsStream(
            "/json/entities/contextual/organization.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.convertValue(organization, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
