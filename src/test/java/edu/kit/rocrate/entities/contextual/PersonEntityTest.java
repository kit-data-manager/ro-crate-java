package edu.kit.rocrate.entities.contextual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import java.io.InputStream;

import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class PersonEntityTest {

  @Test
  void personSerialization() throws IOException {
    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setId("https://orcid.org/0000-0001-6121-5409")
        .setContactPoint("mailto:tim.luckett@uts.edu.au")
        .setAffiliation("https://ror.org/03f0f6041")
        .setFamilyName("Luckett")
        .setGivenName("Tim")
        .addProperty("name", "Tim Luckett")
        .build();

    HelpFunctions.compareEntityWithFile(person, "/json/entities/contextual/person.json");
  }
}
