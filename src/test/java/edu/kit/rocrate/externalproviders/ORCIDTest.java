package edu.kit.rocrate.externalproviders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.externalproviders.personprovider.ORCIDProvider;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.entities.contextual.PersonEntityTest;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 10.2.2022 г.
 * @version 1
 */
public class ORCIDTest {

  @Test
  void testAddingPersonEntity() throws IOException {
    ORCIDProvider orcidProvider = new ORCIDProvider();
    PersonEntity person = orcidProvider.getPerson("https://orcid.org/0000-0001-9842-9718");
    InputStream inputStream =
        PersonEntityTest.class.getResourceAsStream("/json/entities/contextual/orcidperson.json");
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    JsonNode expectedJson = objectMapper.readTree(inputStream);
    JsonNode node = objectMapper.convertValue(person, JsonNode.class);
    assertEquals(node, expectedJson);
  }
}
