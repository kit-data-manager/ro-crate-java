package edu.kit.datamanager.ro_crate.payload;
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

/**
 * Implementation of the Observer pattern, used by the deletion of entities.
 */
public class EntityObserver implements Observer {

  private final RoCratePayload payload;

  public EntityObserver(RoCratePayload payload) {
    this.payload = payload;
  }

  @Override
  public void update(String entityId) {
    this.payload.addToAssociatedItems(this.payload.getEntityById(entityId));
  }
}
