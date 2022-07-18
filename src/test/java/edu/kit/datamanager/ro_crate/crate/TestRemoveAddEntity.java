package edu.kit.datamanager.ro_crate.crate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.PlaceEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestRemoveAddEntity {
  @Test
  void testAddRemoveEntity() throws IOException {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    RoCrate roCrate = new RoCrate.RoCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();
    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/onlyOneFile.json");

    // remove entity and check if equals to the basic crate
    roCrate.deleteEntityById("survey-responses-2019.csv");
    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/simple.json");
  }

  @Test
  void withTwoFiles() throws IOException {
    PlaceEntity place = new PlaceEntity.PlaceEntityBuilder()
        .setId("http://sws.geonames.org/8152662/")
        .addProperty("name", "Catalina Park")
        .build();

    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice")
        .addProperty("description", "One of hopefully many Contextual Entities")
        .build();

    FileEntity file = new FileEntity.FileEntityBuilder()
        .setId("data1.txt")
        .addProperty("description", "One of hopefully many Data Entities")
        .setContentLocation(place.getId())
        .addAuthor(person.getId())
        .build();

    RoCrate roCrate = new RoCrate.RoCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addContextualEntity(place)
        .addContextualEntity(person)
        .addDataEntity(file)
        .addDataEntity(
            new FileEntity.FileEntityBuilder().setId("data2.txt").build()
        )
        .build();

    HelpFunctions.compareCrateJsonToFileInResources(roCrate, "/json/crate/twoFiles.json");

    roCrate.deleteEntityById("data1.txt");
    roCrate.deleteEntityById("data2.txt");
    roCrate.deleteEntityById("#alice");
    roCrate.deleteEntityById("http://sws.geonames.org/8152662/");

    RoCrate roCrate2 = new RoCrate.RoCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity").build();
    HelpFunctions.compareTwoCrateJson(roCrate, roCrate2);
  }

  @Test
  void removeOtherOccur() throws JsonProcessingException {
    PlaceEntity place = new PlaceEntity.PlaceEntityBuilder()
        .setId("http://sws.geonames.org/8152662/")
        .addProperty("name", "Catalina Park")
        .build();

    PersonEntity person = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice")
        .addProperty("description", "One of hopefully many Contextual Entities")
        .build();

    FileEntity file = new FileEntity.FileEntityBuilder()
        .setId("data1.txt")
        .addProperty("description", "One of hopefully many Data Entities")
        .setContentLocation(place.getId())
        .addAuthor(person.getId())
        .addAuthor(person.getId())
        .build();

    RoCrate roCrate = new RoCrate.RoCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addContextualEntity(place)
        .addContextualEntity(person)
        .addDataEntity(file)
        .build();

    roCrate.deleteEntityById(person.getId());
    roCrate.deleteEntityById(place.getId());

    FileEntity file2 = new FileEntity.FileEntityBuilder()
        .setId("data1.txt")
        .addProperty("description", "One of hopefully many Data Entities")
        .build();
    RoCrate second2 = new RoCrate.RoCrateBuilder("Example RO-Crate", "The RO-Crate Root Data Entity")
        .addDataEntity(file2)
        .build();

    HelpFunctions.compareTwoCrateJson(roCrate, second2);
  }
}
