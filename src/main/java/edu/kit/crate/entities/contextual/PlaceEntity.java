package edu.kit.crate.entities.contextual;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class PlaceEntity extends ContextualEntity {

  private static final String TYPE = "Place";

  public PlaceEntity(APlaceEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.addIdProperty("geo", entityBuilder.geo);
    this.addType(TYPE);
  }

  abstract static class APlaceEntityBuilder<T extends APlaceEntityBuilder<T>> extends
      AContextualEntityBuilder<T> {

    String geo;

    // that is the geolocation property
    public T setGeo(String geo) {
      this.geo = geo;
      return self();
    }

    public T setGeo(ContextualEntity geo) {
      this.geo = geo.getId();
      return self();
    }

    abstract public PlaceEntity build();
  }

  final static public class PlaceEntityBuilder extends APlaceEntityBuilder<PlaceEntityBuilder> {

    @Override
    public PlaceEntityBuilder self() {
      return this;
    }

    @Override
    public PlaceEntity build() {
      return new PlaceEntity(this);
    }
  }
}
