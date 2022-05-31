package edu.kit.crate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.context.CrateMetadataContext;
import edu.kit.crate.context.RoCrateMetadataContext;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.externalproviders.dataentities.ImportFromDataCite;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.payload.CratePayload;
import edu.kit.crate.payload.RoCratePayload;
import edu.kit.crate.preview.CratePreview;
import edu.kit.crate.special.JsonUtilFunctions;
import edu.kit.crate.validation.JsonSchemaValidation;
import edu.kit.crate.validation.Validator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The class that represents a single ROCrate.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class RoCrate implements Crate {

  private static final String ID = "ro-crate-metadata.json";
  private static final String RO_SPEC = "https://w3id.org/ro/crate/1.1";

  private final CratePayload roCratePayload;
  private CrateMetadataContext metadataContext;
  private CratePreview roCratePreview;
  private RootDataEntity rootDataEntity;
  private ContextualEntity jsonDescriptor;

  private List<File> untrackedFiles;

  @Override
  public CratePreview getPreview() {
    return this.roCratePreview;
  }

  public void setRoCratePreview(CratePreview preview) {
    this.roCratePreview = preview;
  }

  public void setMetadataContext(CrateMetadataContext metadataContext) {
    this.metadataContext = metadataContext;
  }

  public ContextualEntity getJsonDescriptor() {
    return jsonDescriptor;
  }

  public void setJsonDescriptor(ContextualEntity jsonDescriptor) {
    this.jsonDescriptor = jsonDescriptor;
  }

  public RootDataEntity getRootDataEntity() {
    return rootDataEntity;
  }

  public void setRootDataEntity(RootDataEntity rootDataEntity) {
    this.rootDataEntity = rootDataEntity;
  }

  /**
   * Default constructor for creation of an empty crate.
   */
  public RoCrate() {
    this.roCratePayload = new RoCratePayload();
    this.metadataContext = new RoCrateMetadataContext();
    this.untrackedFiles = new ArrayList<>();
  }

  /**
   * A constructor for creating the crate using a Crate builder for easier creation.
   *
   * @param roCrateBuilder the builder to use.
   */
  public RoCrate(RoCrateBuilder roCrateBuilder) {
    this.roCratePayload = roCrateBuilder.payload;
    this.metadataContext = roCrateBuilder.metadataContext;
    this.roCratePreview = roCrateBuilder.preview;
    this.rootDataEntity = roCrateBuilder.rootDataEntity;
    this.jsonDescriptor = roCrateBuilder.jsonDescriptor;
    this.untrackedFiles = roCrateBuilder.untrackedFiles;
    Validator defaultValidation = new Validator(new JsonSchemaValidation());
    defaultValidation.validate(this);
  }

  @Override
  public String getJsonMetadata() {
    ObjectMapper objectMapper = MyObjectMapper.getMapper();
    ObjectNode node = objectMapper.createObjectNode();

    node.setAll(this.metadataContext.getContextJsonEntity());

    var graph = objectMapper.createArrayNode();
    ObjectNode root = objectMapper.convertValue(this.rootDataEntity, ObjectNode.class);
    graph.add(root);

    graph.add(objectMapper.convertValue(this.jsonDescriptor, JsonNode.class));
    if (this.roCratePayload != null && this.roCratePayload.getEntitiesMetadata() != null) {
      graph.addAll(this.roCratePayload.getEntitiesMetadata());
    }
    node.set("@graph", graph);
    return node.toString();
  }

  @Override
  public DataEntity getDataEntityById(java.lang.String id) {
    return this.roCratePayload.getDataEntityById(id);
  }

  @Override
  public List<DataEntity> getAllDataEntities() {
    return this.roCratePayload.getAllDataEntities();
  }

  @Override
  public ContextualEntity getContextualEntityById(String id) {
    return this.roCratePayload.getContextualEntityById(id);
  }

  @Override
  public AbstractEntity getEntityById(String id) {
    return this.roCratePayload.getEntityById(id);
  }

  @Override
  public void addDataEntity(DataEntity entity, Boolean toHasPart) {
    this.metadataContext.checkEntity(entity);
    this.roCratePayload.addDataEntity(entity);
    if (toHasPart) {
      this.rootDataEntity.addToHasPart(entity.getId());
    }
  }

  @Override
  public void addContextualEntity(ContextualEntity entity) {
    this.metadataContext.checkEntity(entity);
    this.roCratePayload.addContextualEntity(entity);
  }

  @Override
  public void deleteEntityById(String entityId) {
    // delete the entity firstly
    this.roCratePayload.removeEntityById(entityId);
    // remove from the root data entity hasPart
    this.rootDataEntity.removeFromHasPart(entityId);
    // remove from the root entity and the file descriptor
    JsonUtilFunctions.removeFieldsWith(entityId, this.rootDataEntity.getProperties());
    JsonUtilFunctions.removeFieldsWith(entityId, this.jsonDescriptor.getProperties());
  }

  @Override
  public void setUntrackedFiles(List<File> files) {
    this.untrackedFiles = files;
  }

  @Override
  public void addFromCollection(Collection<AbstractEntity> entities) {
    this.roCratePayload.addEntities(entities);
  }

  @Override
  public void addItemFromDataCite(String locationUrl) {
    ImportFromDataCite.addDataCiteToCrate(locationUrl, this);
  }

  @Override
  public List<File> getUntrackedFiles() {
    return this.untrackedFiles;
  }

  /**
   * The inner class builder for the easier creation of a ROCrate.
   */
  public static final class RoCrateBuilder {

    CratePayload payload;
    CratePreview preview;
    CrateMetadataContext metadataContext;
    ContextualEntity license;
    RootDataEntity rootDataEntity;
    ContextualEntity jsonDescriptor;
    List<File> untrackedFiles;

    /**
     * The default constructor of a builder.
     *
     * @param name the name of the crate.
     * @param description the description of the crate.
     */
    public RoCrateBuilder(String name, String description) {
      this.payload = new RoCratePayload();
      this.untrackedFiles = new ArrayList<>();
      this.metadataContext = new RoCrateMetadataContext();
      rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
          .addProperty("name", name)
          .addProperty("description", description)
          .build();
      jsonDescriptor = new ContextualEntity.ContextualEntityBuilder()
          .setId(ID)
          .addType("CreativeWork")
          .addIdProperty("about", "./")
          .addIdProperty("conformsTo", RO_SPEC)
          .build();
    }

    /**
     * A default constructor without any params where the root data entity will be plain.
     */
    public RoCrateBuilder() {
      this.payload = new RoCratePayload();
      this.untrackedFiles = new ArrayList<>();
      this.metadataContext = new RoCrateMetadataContext();
      rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
          .build();
      jsonDescriptor = new ContextualEntity.ContextualEntityBuilder()
          .setId(ID)
          .addType("CreativeWork")
          .addIdProperty("about", "./")
          .addIdProperty("conformsTo", RO_SPEC)
          .build();
    }
    /**
     * Adding a data entity to the crate.
     * The important part here is to also add its id to the RootData Entity hasPart.
     *
     * @param dataEntity the DataEntity object.
     * @return returns the builder for further usage.
     */
    public RoCrateBuilder addDataEntity(DataEntity dataEntity) {
      this.payload.addDataEntity(dataEntity);
      this.rootDataEntity.addToHasPart(dataEntity.getId());
      return this;
    }

    public RoCrateBuilder addContextualEntity(ContextualEntity contextualEntity) {
      this.payload.addContextualEntity(contextualEntity);
      return this;
    }

    /**
     * Setting the license of the crate.
     *
     * @param license the license is a contextual entity.
     * @return the builder for further usage.
     */
    public RoCrateBuilder setLicense(ContextualEntity license) {
      this.license = license;
      this.rootDataEntity.addIdProperty("license", license.getId());
      return this;
    }

    public RoCrateBuilder setContext(CrateMetadataContext context) {
      this.metadataContext = context;
      return this;
    }

    public RoCrateBuilder addUrlToContext(java.lang.String url) {
      this.metadataContext.addToContextFromUrl(url);
      return this;
    }

    public RoCrateBuilder addValuePairToContext(java.lang.String key, java.lang.String value) {
      this.metadataContext.addToContext(key, value);
      return this;
    }

    public RoCrateBuilder setPreview(CratePreview preview) {
      this.preview = preview;
      return this;
    }

    public RoCrateBuilder addUntrackedFile(File file) {
      this.untrackedFiles.add(file);
      return this;
    }

    public RoCrate build() {
      return new RoCrate(this);
    }
  }

}
