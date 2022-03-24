package edu.kit.crate.externalproviders.personprovider;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.validation.EntityValidation;
import edu.kit.crate.entities.validation.JsonSchemaValidation;
import edu.kit.crate.objectmapper.MyObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Nikola Tzotchev on 10.2.2022 г.
 * @version 1
 */
public class ORCIDProvider {

  public static PersonEntity getPerson(String url) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    if (!url.matches("https://orcid.org.*")) {
      throw new IllegalArgumentException("Should provide orcid link");
    }
    HttpGet request = new HttpGet(url);
    request.addHeader(HttpHeaders.ACCEPT, "application/ld+json");
    try {
      CloseableHttpResponse response = httpClient.execute(request);
      ObjectMapper objectMapper = MyObjectMapper.getMapper();
      ObjectNode jsonNode = objectMapper.readValue(response.getEntity().getContent(),
          ObjectNode.class);
      jsonNode.remove("@reverse");
      jsonNode.remove("@context");
      ObjectNode node = objectMapper.createObjectNode();
      EntityValidation entityValidation = new EntityValidation(new JsonSchemaValidation());
      var itr = jsonNode.fields();
      while (itr.hasNext()) {
        var element = itr.next();
        if (entityValidation.fieldValidation(element.getValue())) {
          node.set(element.getKey(), element.getValue());
        }
      }
      return new PersonEntity.PersonEntityBuilder().setAll(node).build();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
