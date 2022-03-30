package edu.kit.rocrate.entities.contextual;

import edu.kit.crate.entities.contextual.PersonEntity;

import java.io.IOException;

import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  // test if creating an entity without Id will get a default UUI
  @Test
  void personWithoutId() {
    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setContactPoint("mailto:tim.luckett@uts.edu.au")
        .setAffiliation("https://ror.org/03f0f6041")
        .setFamilyName("Luckett")
        .setGivenName("Tim")
        .addProperty("name", "Tim Luckett")
        .build();

    assertFalse(person.getId().isEmpty());
  }
}
