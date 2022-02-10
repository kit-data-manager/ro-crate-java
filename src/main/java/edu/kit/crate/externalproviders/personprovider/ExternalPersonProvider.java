package edu.kit.crate.externalproviders.personprovider;

import edu.kit.crate.entities.contextual.PersonEntity;

/**
 * @author Nikola Tzotchev on 10.2.2022 г.
 * @version 1
 */
public interface ExternalPersonProvider {
  PersonEntity getPerson(String url);
}
