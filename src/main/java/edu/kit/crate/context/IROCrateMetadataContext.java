package edu.kit.crate.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.AbstractEntity;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface IROCrateMetadataContext {

  public ObjectNode getContextJsonEntity();
  public boolean checkEntity(AbstractEntity entity);
  public void addToContextFromUrl(String url);
  public void addToContext(String key, String value);

}
