package edu.kit.datamanager.ro_crate.externalproviders;

import java.io.IOException;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.entities.contextual.OrganizationEntity;
import edu.kit.datamanager.ro_crate.externalproviders.organizationprovider.RorProvider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nikola Tzotchev on 11.2.2022 г.
 * @version 1
 */
public class RorProviderTest {

  @Test
  void testExternalRorProvider() throws IOException {
    OrganizationEntity organizationEntity = RorProvider.getOrganization("https://ror.org/04t3en479");
    HelpFunctions.compareEntityWithFile(organizationEntity, "/json/entities/contextual/rorkit.json");
  }

  @Test
  void testInvalidRorUrl() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      OrganizationEntity organizationEntity = RorProvider.getOrganization("https://notror.org/04t3en479");
    });
  }

  @Test
  void testInvalidRorId() {
    OrganizationEntity organizationEntity = RorProvider.getOrganization("https://ror.org/42");
    assertNull(organizationEntity);
  }
}
