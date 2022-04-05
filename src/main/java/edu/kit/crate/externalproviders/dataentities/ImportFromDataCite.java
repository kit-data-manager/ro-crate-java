package edu.kit.crate.externalproviders.dataentities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.IROCrate;
import edu.kit.crate.ROCrate;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.externalproviders.organizationprovider.RORProvider;
import edu.kit.crate.externalproviders.personprovider.ORCIDProvider;
import edu.kit.crate.objectmapper.MyObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportFromDataCite {

  public static IROCrate createCrateFromDataCiteResource(String url) {
    IROCrate crate = new ROCrate.ROCrateBuilder("name", "description")
        .build();
    addDataCiteToCrate(url, crate);
    return crate;
  }

  public static void addDataCiteToCrate(String url, IROCrate crate) {
    var entities = getEntitiesFromDataCiteResource(Objects.requireNonNull(getJsonNodeFromDataCite(url)));
    crate.addFromCollection(entities);
  }

  public static IROCrate createCrateFromDataCiteJson(JsonNode json) {
    IROCrate crate = new ROCrate.ROCrateBuilder("name", "description")
        .build();
    addDataCiteToCrateFromJson(json, crate);
    return crate;
  }

  public static void addDataCiteToCrateFromJson(JsonNode json, IROCrate crate) {
    var entities = getEntitiesFromDataCiteResource(json);
    crate.addFromCollection(entities);
  }

  private static JsonNode getJsonNodeFromDataCite(String url) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(url);
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode jsonNode;
    try {
      CloseableHttpResponse response = httpClient.execute(request);
      jsonNode = objectMapper.readValue(response.getEntity().getContent(),
          ObjectNode.class);
      System.out.println(jsonNode.toPrettyString());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return jsonNode;
  }

  private static List<AbstractEntity> getEntitiesFromDataCiteResource(JsonNode jsonNode) {

    List<AbstractEntity> entityList = new ArrayList<>();
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    Map<String, String> relationType = Stream.of(new String[][]{
        {"IsCitedBy", "isPartOf"},
        {"Cites", "citation"},
        {"IsSupplementTo", "isPartOf"},
        {"IsSupplementedBy", "isPartOf"},
        {"IsContinuedBy", "isPartOf"},
        {"Continues", "isBasedOn"},
        {"IsDescribedBy", "hasPart"},
        {"Describes", "isPartOf"},
        {"HasMetadata", "hasPart"},
        {"IsMetadataFor", "isPartOf"},
        {"HasVersion", "hasPart"},
        {"IsVersionOf", "isPartOf"},
        {"IsNewVersionOf", "isBasedOn"},
        {"IsPreviousVersionOf", "PredecessorOf"},
        {"IsPartOf", "isPartOf"},
        {"HasPart", "hasPart"},
        {"IsPublishedIn", "isPartOf"},
        {"IsReferencedBy", "isPartOf"},
        {"References", "citation"},
        {"IsDocumentedBy", "hasPart"},
        {"Documents", "isPartOf"},
        {"IsCompiledBy", "hasPart"},
        {"Compiles", "isPartOf"},
        {"IsVariantFormOf", "isBasedOn"},
        {"IsOriginalFormOf", "sameAs"},
        {"IsIdenticalTo", "sameAs"},
        {"IsReviewedBy", "hasPart"},
        {"Reviews", "isPartOf"},
        {"IsDerivedFrom", "isBasedOn"},
        {"IsSourceOf", "isBasedOn"},
        {"IsRequiredBy", "isPartOf"},
        {"Requires", "hasPart"},
        {"IsObsoletedBy", "hasPart"},
        {"Obsoletes", "isBasedOn"}
    }).collect(Collectors.collectingAndThen(
        Collectors.toMap(data -> data[0], data -> data[1]),
        Collections::<String, String>unmodifiableMap));

    Collection<AbstractEntity> citation = new HashSet<>();
    Collection<AbstractEntity> hasPart = new HashSet<>();
    Collection<AbstractEntity> IsBasedOn = new HashSet<>();
    Collection<AbstractEntity> IsPartOf = new HashSet<>();
    Collection<AbstractEntity> sameAs = new HashSet<>();


    // the id of the main entity
    String id = null;
    var identifiers = jsonNode.get("identifiers");
    if (identifiers != null) {
      if (identifiers.isEmpty()) {
        id = jsonNode.get("id").asText();
      } else {
        id = identifiers.get(0).get("identifier").asText();
      }
    }

    // main entity type
    String type = "CreativeWork";
    JsonNode types = jsonNode.get("types");
    var schemaType = types.get("schemaOrg");
    if (schemaType != null) {
      type = schemaType.asText();
    }

    // the title
    String title;
    JsonNode titles = jsonNode.get("titles");
    if (titles == null || titles.isEmpty()) {
      // no title
      title = null;
    } else {
      title = titles.get(0).get("title").asText();
    }

    // publicationYear
    String publicationYear;
    JsonNode pYear = jsonNode.get("publicationYear");
    publicationYear = pYear != null ? pYear.asText() : null;

    // the creators + their affiliations
    JsonNode creators = jsonNode.get("creators");
    List<AbstractEntity> creatorsList = new ArrayList<>();
    if (creators != null) {
      for (var e : creators) {
        var person = addPersonToListOfEntities(e, entityList);
        creatorsList.add(person);
      }
    }

    // the publisher
    OrganizationEntity publisher = null;
    JsonNode publisherNode = jsonNode.get("publisher");
    if (publisherNode != null && publisherNode.isTextual()) {
      publisher = new OrganizationEntity.OrganizationEntityBuilder()
          .addProperty("name", publisherNode.asText()).build();
      entityList.add(publisher);
    }

    // contributors
    JsonNode contributors = jsonNode.get("contributors");
    List<AbstractEntity> contributorsList = new ArrayList<>();
    if (contributors != null) {
      for (var e : contributors) {
        var contributor = addPersonToListOfEntities(e, entityList);
        contributorsList.add(contributor);
      }
    }

    // description
    ArrayNode descriptionArray = objectMapper.createArrayNode();
    JsonNode descriptionNode = jsonNode.get("descriptions");
    if (descriptionNode != null) {
      for (var e : descriptionNode) {
        var desc = e.get("description");
        descriptionArray.add(desc.asText());
      }
    }

    // dates
    String dateCreated = null;
    String dateModified = null;
    String datePublished = null;
    JsonNode dates = jsonNode.get("dates");
    if (dates != null) {
      for (var el : dates) {
        String dataType = el.get("dateType").asText();
        String dateString = el.get("date").asText();
        switch (dataType) {
          case "Created":
            dateCreated = dateString;
            break;
          case "Updated":
            dateModified = dateString;
            break;
          case "Available":
            datePublished = dateString;
            break;
          case "Issued":
            if (datePublished == null) {
              datePublished = dateString;
            }
            break;
        }
      }
    }

    // subjects which are mapped to keywords
    JsonNode subjects = jsonNode.get("subjects");
    ArrayNode keyWordsArray = objectMapper.createArrayNode();
    if (subjects != null) {
      for (var sub : subjects) {
        keyWordsArray.add(sub.get("subject").asText());
      }
    }

    Consumer<DataEntity> addRelatedEntity = (entity) -> {
      var mapEntry = relationType.get(entity.getProperty("keywords").asText());
      if (mapEntry != null) {
        switch (mapEntry) {
          case "citation":
            citation.add(entity);
            break;
          case "isBasedOn":
            IsBasedOn.add(entity);
            break;
          case "isPartOf":
            IsPartOf.add(entity);
            break;
          case "sameAs":
            sameAs.add(entity);
            break;
          default:
            hasPart.add(entity);
            break;
        }
      }
    };

    // relatedIdentifiers
    JsonNode related = jsonNode.get("relatedIdentifiers");
    if (related != null) {
      for (var element : related) {
        String relatedId = element.get("relatedIdentifier").asText();
        String relatedIdType = element.get("relatedIdentifierType").asText();
        String relationTypeString = element.get("relationType").asText();
        DataEntity relatedItem = new DataEntity.DataEntityBuilder()
            .addType("CreativeWork")
            .setId(relatedId)
            .addProperty("description", relatedIdType)
            .addProperty("keywords", relationTypeString)
            .build();
        entityList.add(relatedItem);

        addRelatedEntity.accept(relatedItem);
      }
    }


    // geolocations
    JsonNode geo = jsonNode.get("geoLocations");
    Collection<AbstractEntity> geoSet = new HashSet<>();
    if (geo != null) {
      for (var geoThing : geo) {
        // geo point
        ContextualEntity geoEntity = new ContextualEntity.ContextualEntityBuilder()
            .addType("Place")
            .build();

        if (geoThing.get("geoLocationPlace") != null) {
          var placeName = geoThing.get("geoLocationPlace").asText();
          geoEntity.addProperty("name", placeName);
        }

        if (geoThing.get("geoLocationPoint") != null) {
          var point = geoThing.get("geoLocationPoint");
          var geoCoordinate = new ContextualEntity.ContextualEntityBuilder()
              .addType("GeoCoordinates")
              .addProperty("latitude", point.get("pointLatitude"))
              .addProperty("longitude", point.get("pointLongitude"))
              .build();
          entityList.add(geoCoordinate);
          geoEntity.addIdProperty("geo", geoCoordinate.getId());
        }
        String boxString = null;
        String polygonString = null;
        if (geoThing.get("geoLocationBox") != null) {
          var box = geoThing.get("geoLocationBox");
          String bottomLeftLongitude = box.get("westBoundLongitude").asText();
          String bottomLeftLatitude = box.get("southBoundLatitude").asText();

          String topRightLongitude = box.get("eastBoundLongitude").asText();
          String topRightLatitude = box.get("northBoundLatitude").asText();

          boxString = bottomLeftLongitude + "," + bottomLeftLatitude + " " + topRightLongitude + "," + topRightLatitude;

        }
        if (geoThing.get("geoLocationPolygon") != null) {
          StringBuilder stringBuilder = new StringBuilder();
          for (var polygonEl : geoThing.get("geoLocationPolygon")) {
            var point = polygonEl.get("polygonPoint");
            String longitude = point.get("pointLongitude").asText();
            String latitude = point.get("pointLatitude").asText();
            stringBuilder.append(longitude).append(",").append(latitude).append(" ");
          }
          polygonString = stringBuilder.toString();
        }

        if (boxString != null || polygonString != null) {
          ContextualEntity shapeEntity = new ContextualEntity.ContextualEntityBuilder()
              .addType("GeoShape")
              .addProperty("box", boxString)
              .addProperty("box", polygonString)
              .build();
          entityList.add(shapeEntity);
          geoEntity.addIdProperty("geo", shapeEntity.getId());
        }
        entityList.add(geoEntity);
        geoSet.add(geoEntity);
      }
    }

    // language
    String language = null;
    if (jsonNode.get("language") != null) {
      language = jsonNode.get("language").asText();
    }

    // alternateIdentifier
    ArrayNode alternate = objectMapper.createArrayNode();
    if (jsonNode.get("alternateIdentifiers") != null) {
      for (var el : jsonNode) {
        alternate.add(el.get("alternateIdentifier").asText());
      }
    }

    // size
    ArrayNode sizes = null;
    if (jsonNode.get("sizes") != null) {
      sizes = (ArrayNode) jsonNode.get("sizes");
    }

    // format
    ArrayNode formats = null;
    if (jsonNode.get("formats") != null) {
      formats = (ArrayNode) jsonNode.get("formats");
    }

    // rights
    Set<AbstractEntity> licenses = new HashSet<>();
    if (jsonNode.get("rightsList") != null) {
      for (var element : jsonNode.get("rightsList")) {
        String licenseName = null;
        if (element.get("rights") != null) {
          licenseName = element.get("rights").asText();
        }

        String rightsUri = null;
        if (element.get("rightsUri") != null) {
          rightsUri = element.get("rightsUri").asText();
        }

        String rightsIdentifier = null;
        if (element.get("rightsIdentifier") != null) {
          rightsIdentifier = element.get("rightsIdentifier").asText();
        }

        ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
            .addType("CreativeWork")
            .addProperty("name", licenseName)
            .setId(rightsUri)
            .addProperty("identifier", rightsIdentifier)
            .build();

        entityList.add(license);
        licenses.add(license);
      }
    }

    // funding
    String fundingNameString = "fundingReferences";
    Set<AbstractEntity> funderSet = new HashSet<>();
    Set<AbstractEntity> grandSet = new HashSet<>();
    if (jsonNode.get(fundingNameString) != null) {

      for (var el : jsonNode.get(fundingNameString)) {
        var funderId = el.get("funderIdentifierType");
        PersonEntity funder;
        if (funderId != null && funderId.asText().equals("ORCID")) {
          funder = ORCIDProvider.getPerson(funderId.asText());
        } else {
          funder = new PersonEntity.PersonEntityBuilder()
              .addProperty("name", el.get("funderName"))
              .setId(el.get("funderIdentifier") == null ? null : el.get("funderIdentifier").asText())
              .build();
        }
        funderSet.add(funder);
        entityList.add(funder);

        // grants
        var awardUri = el.get("awardUri");
        var awardTitle = el.get("awardTitle");
        var awardNumber = el.get("awardNumber");
        if (awardNumber != null || awardTitle != null || awardUri != null) {
          ContextualEntity grant = new ContextualEntity.ContextualEntityBuilder()
              .addType("Grant")
              .setId(awardUri == null ? null : awardUri.asText())
              .addProperty("name", awardTitle)
              .addProperty("identifier", awardNumber)
              .addIdProperty("funder", funder)
              .addIdProperty("fundedItem", id)
              .build();
          entityList.add(grant);
          grandSet.add(grant);
        }
      }
    }

    // relatedItem
    var relatedItems = jsonNode.get("relatedItems");
    if (relatedItems != null) {
      for (var relatedItem : relatedItems) {
        DataEntity entity = getFromRelatedItem(relatedItem);
        entityList.add(entity);
        addRelatedEntity.accept(entity);
      }
    }
    DataEntity dataEntity = new DataEntity.DataEntityBuilder()
        .setId(id)
        .addType(type)
        .addProperty("title", title)
        .addProperty("publicationYear", publicationYear)
        .addIdProperty("publisher", publisher)
        .addIdFromCollectionOFEntities("creator", creatorsList)
        .addIdFromCollectionOFEntities("contributor", contributorsList)
        .addIdFromCollectionOFEntities("spatialCoverage", geoSet)
        .addIdFromCollectionOFEntities("license", licenses)
        .addIdFromCollectionOFEntities("funder", funderSet)
        .addIdFromCollectionOFEntities("funding", grandSet)
        .addIdFromCollectionOFEntities("isPartOf", IsPartOf)
        .addIdFromCollectionOFEntities("citation", citation)
        .addIdFromCollectionOFEntities("hasPart", hasPart)
        .addIdFromCollectionOFEntities("isBasedOn", IsBasedOn)
        .addIdFromCollectionOFEntities("sameAs", sameAs)
        .addProperty("description", descriptionArray)
        .addProperty("dateCreated", dateCreated)
        .addProperty("dateModified", dateModified)
        .addProperty("datePublished", datePublished)
        .addProperty("keywords", keyWordsArray)
        .addProperty("inLanguage", language)
        .addProperty("identifier", alternate)
        .addProperty("size", sizes)
        .addProperty("encodingFormat", formats)
        .addProperty("version", jsonNode.get("version"))
        .build();

    entityList.add(dataEntity);
    return entityList;
  }

  // here one can map also all the other properties of relatedItem if necessary
  private static DataEntity getFromRelatedItem(JsonNode relatedItem) {
    var relatedId = relatedItem.get("relatedIdentifier");
    String relatedIdType = relatedItem.get("relatedIdentifierType").asText();
    String relationTypeString = relatedItem.get("relationType").asText();
    return new DataEntity.DataEntityBuilder()
        .addType("CreativeWork")
        .setId(relatedId == null ? null : relatedId.asText())
        .addProperty("description", relatedIdType)
        .addProperty("keywords", relationTypeString)
        .build();
  }

  private static AbstractEntity addPersonToListOfEntities(JsonNode personNode, Collection<AbstractEntity> entities) {
    // if there is an Orcid provider use it to get the entity directly from orcid
    var creatorIdentifiers = personNode.get("nameIdentifiers");
    PersonEntity person = null;
    String personUrl = null;
    if (creatorIdentifiers != null && !creatorIdentifiers.isEmpty()) {
      for (var identifier : creatorIdentifiers) {
        if (identifier.get("nameIdentifierScheme").asText().equals("ORCID")) {
          person = ORCIDProvider.getPerson(identifier.get("nameIdentifier").asText());
        } else {
          personUrl = identifier.get("nameIdentifier").asText();
        }
      }
    }

    if (person == null) {
      person = new PersonEntity.PersonEntityBuilder()
          .addProperty("name", personNode.get("creatorName"))
          .addProperty("givenName", personNode.get("givenName"))
          .addProperty("familyName", personNode.get("familyName"))
          .addProperty("url", personUrl)
          .build();
    }

    // get the person affiliations
    Set<OrganizationEntity> organizationEntitySet = new HashSet<>();

    var affiliationArray = personNode.get("affiliation");
    if (affiliationArray != null) {
      for (var affiliationElement : affiliationArray) {
        var affiliationName = affiliationElement.get("name");
        var affiliationId = affiliationElement.get("affiliationIdentifier");
        var scheme = affiliationElement.get("affiliationIdentifierScheme");
        // if the scheme is ROR we can get the entity from there.
        OrganizationEntity organization;
        if (scheme != null && scheme.asText().equals("ROR")) {
          organization = RORProvider.getOrganization(affiliationId.asText());
        } else {
          organization = new OrganizationEntity.OrganizationEntityBuilder()
              .setId(affiliationId.asText())
              .addProperty("name", affiliationName)
              .build();
        }
        organizationEntitySet.add(organization);
      }
    }
    person.addIdListProperties("affiliation", organizationEntitySet.stream().map(AbstractEntity::getId).collect(Collectors.toList()));
    // person.addIdListProperties();
    entities.addAll(organizationEntitySet);
    entities.add(person);
    return person;
  }
}