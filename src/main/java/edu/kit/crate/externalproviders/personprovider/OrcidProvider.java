package edu.kit.crate.externalproviders.personprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.entities.validation.EntityValidation;
import edu.kit.crate.entities.validation.JsonSchemaValidation;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Class for creating person entities from orcid uri.
 *
 * @author Nikola Tzotchev on 10.2.2022 г.
 * @version 1
 */
public class OrcidProvider {

  /**
   * Static method for importing a person entity from his ORCID id.
   *
   * @param url the url of the orcid identifier of the person.
   * @return the created PersonEntity.
   */
  public static PersonEntity getPerson(String url) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    if (!url.startsWith("https://orcid.org")) {
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
