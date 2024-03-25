package edu.kit.datamanager.ro_crate.entities.contextual;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import edu.kit.datamanager.ro_crate.HelpFunctions;

import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class ContextualEntityTest {

  /**
   * This is exactly the same test as the PlaceEntityTest it shows again (similarly to the
   * DataEntityTest) that we can also just use this class for the creation of our Contextual
   * Entities
   */
  @Test
  void testSerialization() throws IOException {
    // this does not make any difference for our testcase it just shows how the GeoCoordinates entity will look
    ContextualEntity geo = new ContextualEntity.ContextualEntityBuilder()
        .addId("#b4168a98-8534-4c6d-a568-64a55157b656")
        .addType("GeoCoordinates")
        .addProperty("latitude", "-33.7152")
        .addProperty("longitude", "150.30119")
        .addProperty("name", "Latitude: -33.7152 Longitude: 150.30119")
        .build();

    ContextualEntity place = new ContextualEntity.ContextualEntityBuilder()
        .addType("Place")
        .addId("https://sws.geonames.org/8152662/")
        .addProperty("description",
            "Catalina Park is a disused motor racing venue, located at Katoomba ...")
        .addProperty("identifier", "https://sws.geonames.org/8152662/")
        .addProperty("uri", "https://www.geonames.org/8152662/catalina-park.html")
        .addProperty("name", "Catalina Park")
        // here we can also do .setGeo(geo)
        .addIdProperty("geo", geo)
        .build();

    assertTrue(place.getLinkedTo().contains(geo.getId()));
    HelpFunctions.compareEntityWithFile(place, "/json/entities/contextual/place.json");
  }
}
