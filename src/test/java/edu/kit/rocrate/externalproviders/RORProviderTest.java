package edu.kit.rocrate.externalproviders;

import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.externalproviders.organizationprovider.RORProvider;
import java.io.IOException;

import edu.kit.rocrate.HelpFunctions;
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
    HelpFunctions.compareEntityWithFile(organizationEntity, "/json/entities/contextual/rorkit.json");
  }
}
