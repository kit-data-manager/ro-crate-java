package edu.kit.crate.entities.contextual;

import java.io.IOException;

import edu.kit.crate.HelpFunctions;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class PlaceEntityTest {

  @Test
  void testSerialization() throws IOException {
    // this does not make any difference for our testcase it just shows how the GeoCoordinates entity will look
    ContextualEntity geo = new ContextualEntity.ContextualEntityBuilder()
        .addType("GeoCoordinates")
        .addProperty("latitude", "-33.7152")
        .addProperty("longitude", "150.30119")
        .addProperty("name", "Latitude: -33.7152 Longitude: 150.30119")
        .build();

    PlaceEntity place = new PlaceEntity.PlaceEntityBuilder()
        .setId("https://sws.geonames.org/8152662/")
        .addProperty("description",
            "Catalina Park is a disused motor racing venue, located at Katoomba ...")
        .addProperty("identifier", "https://sws.geonames.org/8152662/")
        .addProperty("uri", "https://www.geonames.org/8152662/catalina-park.html")
        .addProperty("name", "Catalina Park")
        // here we can also do .setGeo(geo)
        .setGeo("#b4168a98-8534-4c6d-a568-64a55157b656")
        .build();

    HelpFunctions.compareEntityWithFile(place, "/json/entities/contextual/place.json");
  }
}
