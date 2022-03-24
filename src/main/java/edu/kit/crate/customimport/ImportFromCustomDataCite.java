package edu.kit.crate.customimport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.IROCrate;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.writer.FolderWriter;
import edu.kit.crate.writer.ROCrateWriter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImportFromCustomDataCite {

  public static void addDataCiteResource(String url, IROCrate crate) {

    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(url);
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode jsonNode;
    try {
      CloseableHttpResponse response = httpClient.execute(request);
      jsonNode = objectMapper.readValue(response.getEntity().getContent(),
          ObjectNode.class);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    // identifier property
    String id = null;
    if (!jsonNode.get("identifier").isNull()) {
      id = jsonNode.get("identifier").get("value").asText();
      if (id.equals("(:tba)")) {
        id = jsonNode.get("id").asText();
      }
    }
    // title property
    List<String> name = new ArrayList<>();
    if (jsonNode.get("titles").isArray()) {
      for (var e : jsonNode.get("titles")) {
        if (!e.get("value").isNull()) {
          name.add(e.get("value").asText());
        }
      }
    }
    // creator property
    List<PersonEntity> creators = new ArrayList<>();
    if (jsonNode.get("creators").isArray()) {
      for (var creator : jsonNode.get("creators")) {
        if (!creator.isNull()) {
          creators.add(getPerson(creator));
        }
      }
    }
    // publisher
    String publisher = jsonNode.get("publisher").asText();
    // year
    String datePublished = jsonNode.get("publicationYear").asText();
    // get type
    List<String> types = new ArrayList<>();
    if (!jsonNode.get("resourceType").isNull()) {
      String type = jsonNode.get("resourceType").get("typeGeneral").asText();
      switch (type) {
        case "AUDIOVISUAL":
        case "INTERACTIVE_RESOURCE":
        case "PHYSICAL_OBJECT":
        case "OTHER":
          types.add("CreativeWork");
          break;
        case "COLLECTION":
          types.add("Collection");
          break;
        case "DATASET":
          types.add("Dataset");
          break;
        case "EVENT":
          types.add("Event");
          break;
        case "IMAGE":
        case "MODEL":
        case "TEXT":
          types.add("File");
          break;
        case "SERVICE":
          types.add("Service");
          break;
        case "SOFTWARE":
          types.add("SoftwareApplication");
          break;
        case "SOUND":
          types.add("AudioObject");
          break;
        case "WORKFLOW":
          types.add("File");
          types.add("SoftwareSourceCode");
          types.add("ComputationalWorkflow");
          break;
      }
    }

    //contributors
    List<PersonEntity> contributors = new ArrayList<>();
    if (jsonNode.get("contributors").isArray()) {
      for (var el : jsonNode.get("contributors")) {
        contributors.add(getPerson(el.get("user")));
      }
    }
    // dates
    String dateCreated = null;
    String dateModified = null;
    if (jsonNode.get("dates").isArray()) {
      for (var element : jsonNode.get("dates")) {
        // search for date created and date modified
        var e = element.get("type");
        if (e.asText().equals("CREATED")) {
          dateCreated = element.get("value").asText();
        } else if (e.asText().equals("UPDATED")) {
          dateModified = element.get("value").asText();
        }
      }
    }
    // related identifiers
    List<String> isPartOf = new ArrayList<>();
    if (jsonNode.get("relatedIdentifiers").isArray()) {
      for (var element : jsonNode.get("relatedIdentifiers")) {
        var val = element.get("value");
        if (!val.isNull()) {
          isPartOf.add(val.asText());
          //addDataCiteResource(val.asText(), crate);
        }
      }
    }

    // gather all the descriptions in a single string
    StringBuilder descriptionBuilder = new StringBuilder();
    if (jsonNode.get("descriptions").isArray()) {
      for (var element : jsonNode.get("descriptions")) {
        var e = element.get("description");
        if (!e.isNull())
          descriptionBuilder.append(e.asText());
      }
    }
    String description = descriptionBuilder.length() == 0 ? null : descriptionBuilder.toString();

    //language
    String language = null;
    if (!jsonNode.get("language").isNull()) {
      language = jsonNode.get("language").asText();
    }


    // alternateIdentifiers
    List<String> identifiers = new ArrayList<>();
    if (jsonNode.get("alternateIdentifiers").isArray()) {
      for (var element : jsonNode.get("alternateIdentifiers")) {
        var e = element.get("value");
        if (!e.isNull())
          identifiers.add(e.asText());
      }
    }

    //sizes
    JsonNode sizes = null;
    if (jsonNode.get("sizes").isArray()) {
      sizes = jsonNode.get("sizes");
    }
    //formats
    JsonNode formats = null;
    if (jsonNode.get("formats").isArray()) {
      formats = jsonNode.get("formats");
    }
    // version
    String version = null;
    if (!jsonNode.get("version").isNull()) {
      version = jsonNode.get("version").asText();
    }
    // funder
    // create organization entity for each funder
    List<OrganizationEntity> funders = new ArrayList<>();
    if (jsonNode.get("fundingReferences").isArray()) {
      for (var element : jsonNode.get("fundingReferences")) {
        var funderName = element.get("funderName");
        var funderId = jsonNode.get("funderIdentifier").get("value");
        OrganizationEntity organization = new OrganizationEntity.OrganizationEntityBuilder()
            .setId(funderId.isNull() ? element.get("id").asText() : funderId.asText())
            .addProperty("name", funderName.isNull() ? null : funderName.asText())
            .build();
        funders.add(organization);
      }
    }

    // geolocation
    List<ContextualEntity> geoLocations = new ArrayList<>();
    if (jsonNode.get("geoLocations").isArray()) {
      for (var element : jsonNode.get("geoLocations")) {
        var address = element.get("place");
        if (!element.get("point").isNull()) {
          var latitude = element.get("point").get("latitude");
          var longitude = element.get("point").get("longitude");
          ContextualEntity contextualEntity = new ContextualEntity.ContextualEntityBuilder()
              .addType("GeoCoordinates")
              .setId(element.get("id").asText())
              .addProperty("address", address.isNull() ? null : address.asText())
              .addProperty("latitude", latitude.isNull() ? null : latitude.asText())
              .addProperty("longitude", longitude.isNull() ? null : longitude.asText())
              .build();
          geoLocations.add(contextualEntity);
        }
      }
    }

    // the main data entity
    DataEntity data = new DataEntity.DataEntityBuilder()
        .setId(id)
        .addTypes(types)
        .addProperty("name", objectMapper.valueToTree(name))
        .addProperty("publisher", publisher)
        .addProperty("datePublished", datePublished)
        .addProperty("dateCreated", dateCreated)
        .addProperty("dateModified", dateModified)
        .addProperty("inLanguage", language)
        .addProperty("identifier", objectMapper.valueToTree(identifiers))
        .addProperty("version", version)
        .addProperty("size", sizes)
        .addProperty("fileFormat", formats)
        .build();

    List<String> creatorsId = creators.stream().map(AbstractEntity::getId).collect(Collectors.toList());
    data.addIdListProperties("author", creatorsId);
    creators.forEach(crate::addContextualEntity);

    List<String> contributorsId = contributors.stream().map(AbstractEntity::getId).collect(Collectors.toList());
    data.addIdListProperties("contributor", contributorsId);
    contributors.forEach(crate::addContextualEntity);

    List<String> funderId = funders.stream().map(AbstractEntity::getId).collect(Collectors.toList());
    data.addIdListProperties("funder", funderId);
    funders.forEach(crate::addContextualEntity);

    List<String> geoId = geoLocations.stream().map(AbstractEntity::getId).collect(Collectors.toList());
    data.addIdListProperties("contentLocation", geoId);
    geoLocations.forEach(crate::addContextualEntity);

    data.addIdListProperties("isPartOf", isPartOf);
    crate.addDataEntity(data, true);

    // als add a https://schema.org/MediaObject to the crate that indicates the location where to download the files.
    DataEntity mediaObject = new DataEntity.DataEntityBuilder()
        .addType("MediaObject")
        .setId(id + "-data")
        .addProperty("encodingFormat", "application/zip")
        .addProperty("contentUrl", url + "/data")
        .addProperty("description", "The zip file that contain the content associated with this data recourse")
        .build();

    crate.addDataEntity(mediaObject, true);
    data.addIdProperty("hasPart", mediaObject.getId());
    ROCrateWriter writer = new ROCrateWriter(new FolderWriter());
    writer.save(crate, "test");
  }

  private static PersonEntity getPerson(JsonNode personNode) {
    var givenName= personNode.get("givenName");
    var familyName = personNode.get("familyName");
    return new PersonEntity.PersonEntityBuilder()
        .setId(personNode.get("id").asText())
        .setGivenName(givenName.isNull() ? null : givenName.asText())
        .setFamilyName(familyName.isNull() ? null : familyName.asText())
        .addProperty("affiliation", personNode.get("affiliation"))
        .build();
  }
}
