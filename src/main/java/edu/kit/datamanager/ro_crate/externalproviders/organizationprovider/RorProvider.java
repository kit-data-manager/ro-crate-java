package edu.kit.datamanager.ro_crate.externalproviders.organizationprovider;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.entities.contextual.OrganizationEntity;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing possibility to import organization entities from ror.com.
 */
public class RorProvider {

  private static Logger logger = LoggerFactory.getLogger(RorProvider.class);

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

    try (
      CloseableHttpClient httpClient = HttpClients.createDefault();
      CloseableHttpResponse response = httpClient.execute(request);
    ) {
        boolean isError = response.getStatusLine().getStatusCode() != HttpStatus.SC_OK;
        if (isError) {
          String errorMessage = String.format("Identifier not found: %s", response.getStatusLine().toString());
          logger.error(errorMessage);
          return null;
        }
        ObjectNode jsonNode = MyObjectMapper.getMapper().readValue(response.getEntity().getContent(),
            ObjectNode.class);
        return new OrganizationEntity.OrganizationEntityBuilder()
            .addId(jsonNode.get("id").asText())
            .addProperty("name", jsonNode.get("name"))
            .addProperty("email", jsonNode.get("email_address"))
            .addProperty("url", jsonNode.get("links"))
            .build();
    } catch (IOException e) {
      String errorMessage = String.format("IO error: %s", e.getMessage());
      logger.error(errorMessage);
    }
    return null;
  }
}
