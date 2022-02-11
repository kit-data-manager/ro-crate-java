package edu.kit.crate.externalproviders.organizationprovider;

import edu.kit.crate.entities.contextual.OrganizationEntity;

/**
 * @author Nikola Tzotchev on 11.2.2022 г.
 * @version 1
 */
public interface IExternalOrganizationProvider {
  OrganizationEntity getOrganization(String url);
}
