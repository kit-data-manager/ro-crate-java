package edu.kit.crate.externalproviders;

import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.externalproviders.personprovider.OrcidProvider;
import edu.kit.crate.HelpFunctions;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 10.2.2022 г.
 * @version 1
 */
public class ORCIDTest {

  @Test
  void testAddingPersonEntity() throws IOException {
    PersonEntity person = OrcidProvider.getPerson("https://orcid.org/0000-0001-9842-9718");
    HelpFunctions.compareEntityWithFile(person, "/json/entities/contextual/orcidperson.json");
  }
}
