package edu.kit.crate.entities.contextual;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class OrganizationEntity extends ContextualEntity {

  private static final String TYPE = "Organization";

  public OrganizationEntity(AOrganizationEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.addProperty("address", entityBuilder.address);
    this.addProperty("email", entityBuilder.email);
    this.addProperty("telephone", entityBuilder.telephone);
    this.addIdProperty("location", entityBuilder.locationId);
    this.addType(TYPE);
  }

  abstract static class AOrganizationEntityBuilder<T extends AOrganizationEntityBuilder<T>> extends
      AContextualEntityBuilder<T> {

    String address;
    String email;
    String telephone;
    String locationId;

    public T setAddress(String address) {
      this.address = address;
      return self();
    }

    public T setEmail(String email) {
      this.email = email;
      return self();
    }

    public T setTelephone(String telephone) {
      this.telephone = telephone;
      return self();
    }

    public T setLocationId(String placeId) {
      this.locationId = placeId;
      return self();
    }

    @Override
    abstract public OrganizationEntity build();
  }

  final static public class OrganizationEntityBuilder extends
      AOrganizationEntityBuilder<OrganizationEntityBuilder> {

    @Override
    public OrganizationEntityBuilder self() {
      return this;
    }

    @Override
    public OrganizationEntity build() {
      return new OrganizationEntity(this);
    }
  }
}
