package edu.kit.crate.externalproviders.organizationprovider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.contextual.OrganizationEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Nikola Tzotchev on 11.2.2022 г.
 * @version 1
 */
public class RORProvider implements
    IExternalOrganizationProvider {

  @Override
  public OrganizationEntity getOrganization(String url) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    if (!url.matches("https://ror.org/.*")) {
      throw new IllegalArgumentException("Should provide orcid link");
    }
    String newUrl = "https://api.ror.org/organizations/" + url.replaceAll("https://ror.org/","");
    HttpGet request = new HttpGet(newUrl);

    try {
      CloseableHttpResponse response = httpClient.execute(request);
      ObjectNode jsonNode = MyObjectMapper.getMapper().readValue(response.getEntity().getContent(),
          ObjectNode.class);
      return new OrganizationEntity.OrganizationEntityBuilder()
          .setId(jsonNode.get("id").asText())
          .addProperty("name", jsonNode.get("name"))
          .addProperty("email", jsonNode.get("email_address"))
          .addProperty("url", jsonNode.get("links"))
          .build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
