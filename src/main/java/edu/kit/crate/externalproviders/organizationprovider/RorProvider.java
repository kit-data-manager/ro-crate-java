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
 * Class providing possibility to import organization entities from ror.com.
 */
public class RorProvider {

  /**
   * The method that parses a ror entry to a crate entity.
   *
   * @param url the url of the ror entry.
   * @return the created Organization entity.
   */
  public static OrganizationEntity getOrganization(String url) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    if (!url.startsWith("https://ror.org/")) {
      throw new IllegalArgumentException("Should provide ror url");
    }
    String newUrl = "https://api.ror.org/organizations/" + url.replaceAll("https://ror.org/", "");
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
