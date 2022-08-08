package edu.kit.datamanager.ro_crate.context;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;

/**
 * Interface for the metadata context.
 * Most often in an ROCrate this is the default context,
 * but allowed also additional entries can be added.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public interface CrateMetadataContext {

  ObjectNode getContextJsonEntity();

  boolean checkEntity(AbstractEntity entity);

  void addToContextFromUrl(String url);

  void addToContext(String key, String value);

  void deleteValuePairFromContext(String key);
}
