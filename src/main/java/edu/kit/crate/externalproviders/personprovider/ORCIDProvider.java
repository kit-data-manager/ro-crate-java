package edu.kit.crate.externalproviders.personprovider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.contextual.PersonEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
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
    // TODO: check if ulr is orcid url
    HttpGet request = new HttpGet(url);
    request.addHeader(HttpHeaders.ACCEPT, "application/ld+json");
    try {
      CloseableHttpResponse response = httpClient.execute(request);
      ObjectNode jsonNode = MyObjectMapper.getMapper().readValue(response.getEntity().getContent(),
          ObjectNode.class);
      jsonNode.remove("@reverse");
      jsonNode.remove("@context");
      return new PersonEntity.PersonEntityBuilder().setAll(jsonNode).build();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
