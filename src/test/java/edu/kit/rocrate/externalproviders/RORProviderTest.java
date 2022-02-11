package edu.kit.rocrate.externalproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.externalproviders.organizationprovider.RORProvider;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.entities.contextual.PersonEntityTest;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 11.2.2022 г.
 * @version 1
 */
public class RORProviderTest {

  @Test
  void testExternalRORProvider() throws IOException {
    RORProvider rorProvider = new RORProvider();
    OrganizationEntity organizationEntity = rorProvider.getOrganization("https://ror.org/04t3en479");
    InputStream inputStream =
        RORProviderTest.class.getResourceAsStream("/json/entities/contextual/rorkit.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.convertValue(organizationEntity, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
