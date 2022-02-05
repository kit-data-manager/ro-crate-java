package edu.kit.crate.entities.contextual;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class PersonEntity extends ContextualEntity {

  private static final String TYPE = "Person";

  public PersonEntity(APersonEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.addIdProperty("affiliation", entityBuilder.affiliation);
    this.addIdProperty("contactPoint", entityBuilder.contactPoint);
    this.addProperty("email", entityBuilder.email);
    this.addProperty("givenName", entityBuilder.givenName);
    this.addProperty("familyName", entityBuilder.familyName);
    this.addType(TYPE);
  }

  abstract static class APersonEntityBuilder<T extends APersonEntityBuilder<T>> extends
      AContextualEntityBuilder<T> {

    String affiliation;
    String contactPoint;
    String email;
    String givenName;
    String familyName;

    public T setAffiliation(String organisation) {
      this.affiliation = organisation;
      return self();
    }

    public T setContactPoint(String contactPoint) {
      this.contactPoint = contactPoint;
      return self();
    }

    public T setEmail(String email) {
      this.email = email;
      return self();
    }

    public T setGivenName(String name) {
      this.givenName = name;
      return self();
    }

    public T setFamilyName(String familyName) {
      this.familyName = familyName;
      return self();
    }

    @Override
    abstract public PersonEntity build();
  }

  final static public class PersonEntityBuilder extends APersonEntityBuilder<PersonEntityBuilder> {

    @Override
    public PersonEntityBuilder self() {
      return this;
    }

    @Override
    public PersonEntity build() {
      return new PersonEntity(this);
    }
  }
}
