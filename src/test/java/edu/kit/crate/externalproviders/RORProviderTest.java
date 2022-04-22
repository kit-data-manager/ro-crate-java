package edu.kit.crate.externalproviders;

import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.externalproviders.organizationprovider.RorProvider;
import java.io.IOException;

import edu.kit.crate.HelpFunctions;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 11.2.2022 г.
 * @version 1
 */
public class RORProviderTest {

  @Test
  void testExternalRORProvider() throws IOException {
    OrganizationEntity organizationEntity = RorProvider.getOrganization("https://ror.org/04t3en479");
    HelpFunctions.compareEntityWithFile(organizationEntity, "/json/entities/contextual/rorkit.json");
  }
}
