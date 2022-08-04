package edu.kit.datamanager.ro_crate.externalproviders.organizationprovider;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.entities.contextual.OrganizationEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Class providing possibility to import organization entities from ror.com.
 */
public class RorProvider {

  private RorProvider() {}

  /**
   * The method that parses a ror entry to a crate entity.
   *
   * @param url the url of the ror entry.
   * @return the created Organization entity.
   */
  public static OrganizationEntity getOrganization(String url) {
    if (!url.startsWith("https://ror.org/")) {
      throw new IllegalArgumentException("Should provide ror url");
    }
    String newUrl = "https://api.ror.org/organizations/" + url.replaceAll("https://ror.org/", "");
    HttpGet request = new HttpGet(newUrl);

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        CloseableHttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new IOException(String.format("Identifier not found: %s", response.getStatusLine().toString()));
        }
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
