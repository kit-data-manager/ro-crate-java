package edu.kit.rocrate.crate;

import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.contextual.PlaceEntity;
import edu.kit.crate.entities.data.DataSetEntity;
import edu.kit.crate.entities.data.FileEntity;

import java.io.IOException;

import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.rocrate.HelpFunctions;
import org.junit.jupiter.api.Test;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class SerializationTest {

  @Test
  void simpleROCrateSerialization() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .build();
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/simple.json");
  }

  @Test
  void simpleTestWithBonusContextPair() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addValuePairToContext("@test", "ww.test")
        .build();
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/simple2.json");
  }

  @Test
  void onlyOneFile() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/onlyOneFile.json");
  }

  @Test
  void twoSameId() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("minimal", "minimal RO_crate")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "dadadada")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .addProperty("encodingFormat", "text/csv")
                .build()
        )
        .build();
    JsonNode node = MyObjectMapper.getMapper().readTree(roCrate.getJsonMetadata());
    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/onlyOneFile.json");
  }

  @Test
  void twoFilesAndSomeContextualEntities() throws IOException {

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


    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addContextualEntity(place)
        .addContextualEntity(person)
        .addDataEntity(file)
        .addDataEntity(
            new FileEntity.FileEntityBuilder().setId("data2.txt").build()
        )
        .build();

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/twoFiles.json");
  }


  @Test
  void fileAndDirTest() throws IOException {
    ROCrate roCrate = new ROCrate.ROCrateBuilder("Example RO-Crate",
        "The RO-Crate Root Data Entity")
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .addProperty("name", "Too many files")
                .addProperty("description",
                    "This directory contains many small files, that we're not going to describe in detail.")
                .build()
        )
        .build();

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/fileAndDir.json");
  }

  @Test
  void BiggerExample() throws IOException {

    ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
        .setId("https://creativecommons.org/licenses/by/4.0/")
        .addType("CreativeWork")
        .addProperty("name", "CC BY 4.0")
        .addProperty("description", "Creative Commons Attribution 4.0 International License")
        .build();

    ContextualEntity geo = new ContextualEntity.ContextualEntityBuilder()
        .setId("#4241434-33413")
        .addType("GeoCoordinates")
        .addProperty("latitude", "49.00944")
        .addProperty("longitude", "8.41167")
        .build();

    PlaceEntity uni = new PlaceEntity.PlaceEntityBuilder()
        .setId("kit_location")
        .setGeo(geo.getId())
        .build();

    OrganizationEntity organization = new OrganizationEntity.OrganizationEntityBuilder()
        .setId("https://www.geonames.org/7288147")
        .addProperty("name", "Karlsruher Institut fuer Technologie")
        .addProperty("url", "https://www.kit.edu/")
        .setLocationId(uni.getId())
        .build();

    PlaceEntity country = new PlaceEntity.PlaceEntityBuilder()
        .setId("https://www.geonames.org/2921044")
        .addProperty("description", "Big country in central Europe.")
        .build();

    PersonEntity author = new PersonEntity.PersonEntityBuilder()
        .setId("creator")
        .setEmail("uuuuu@student.kit.edu")
        .setFamilyName("Tzotchev")
        .setGivenName("Nikola")
        .setAffiliation(organization.getId())
        .addIdProperty("nationality", country.getId())
        .build();

    ContextualEntity instrument = new ContextualEntity.ContextualEntityBuilder()
        .setId("https://www.aeroqual.com/product/outdoor-portable-monitor-starter-kit")
        .addType("IndividualProduct")
        .addProperty("description",
            "The Outdoor Air Quality Test Kit (Starter) is for users who want an affordable set of tools to measure the common pollutants in ambient outdoor air.")
        .build();

    DataSetEntity measure = new DataSetEntity.DataSetBuilder()
        .setId("measurements/")
        .addProperty("name", "Measurement data.")
        .addProperty("description", "This folder contains all relative to the measurements files.")
        .addAuthor(author.getId())
        .addIdProperty("license", license.getId())
        .build();

    ContextualEntity createAction = new ContextualEntity.ContextualEntityBuilder()
        .setId("#MeasurementCapture_23231")
        .addType("CreateAction")
        .addIdProperty("agent", author.getId())
        .addIdProperty("instrument", instrument.getId())
        .addIdProperty("result", measure.getId())
        .build();

    ROCrate roCrate = new ROCrate.ROCrateBuilder("Air quality measurements in Karlsruhe",
        "Air quality measurements conducted in different places across Karlsruhe")
        .setLicense(license)
        .addContextualEntity(license)
        .addContextualEntity(geo)
        .addContextualEntity(uni)
        .addContextualEntity(organization)
        .addContextualEntity(country)
        .addContextualEntity(author)
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("map.pdf")
                .addProperty("name", "Map of measurements")
                .addProperty("description",
                    "A map of all the location where the tests have been conducted")
                .addProperty("datePublished", "2021-10-22T00:00:00Z")
                .addProperty("encodingFormat", "application/pdf")
                .addAuthor(author.getId())
                .build()
        )
        .addDataEntity(measure)
        .addContextualEntity(instrument)
        .addContextualEntity(createAction)
        .build();

    HelpFunctions.compareTwoMetadataJsonEqual(roCrate, "/json/crate/BiggerExample.json");
  }
}
