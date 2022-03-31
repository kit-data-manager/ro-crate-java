package edu.kit.crate.payload;

public class EntityObserver implements IObserver {

  private ROCratePayload payload;

  public EntityObserver(ROCratePayload payload) {
    this.payload = payload;
  }

  @Override
  public void update(String entityId) {
    this.payload.addToAssociatedItems(this.payload.getEntityById(entityId));
  }
}
