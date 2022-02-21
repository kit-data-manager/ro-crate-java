package edu.kit.crate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.context.ROCrateMetadataContext;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.payload.IROCratePayload;
import edu.kit.crate.payload.ROCratePayload;
import edu.kit.crate.preview.CustomPreview;
import edu.kit.crate.special.JsonHelpFunctions;
import edu.kit.crate.preview.IROCratePreview;
import edu.kit.crate.preview.AutomaticPreview;

import java.util.Collections;
import java.util.List;

/**
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class ROCrate implements IROCrate {

  private final static String ID = "ro-crate-metadata.json";
  private final static String RO_SPEC = "https://w3id.org/ro/crate/1.1";
  private final static String DEFAULT_CONTEXT = "https://w3id.org/ro/crate/1.1/context";
  private IROCratePayload roCratePayload;
  private IROCrateMetadataContext metadataContext;
  private IROCratePreview roCratePreview;
  private RootDataEntity rootDataEntity;
  private DataEntity jsonDescriptor;


  @Override
  public IROCratePreview getPreview() {
    return this.roCratePreview;
  }

  public void setMetadataContext(IROCrateMetadataContext metadataContext) {
    this.metadataContext = metadataContext;
  }

  public DataEntity getJsonDescriptor() {
    return jsonDescriptor;
  }

  public void setJsonDescriptor(DataEntity jsonDescriptor) {
    this.jsonDescriptor = jsonDescriptor;
  }

  public RootDataEntity getRootDataEntity() {
    return rootDataEntity;
  }

  public void setRootDataEntity(RootDataEntity rootDataEntity) {
    this.rootDataEntity = rootDataEntity;
  }

  public ROCrate() {
    this.roCratePayload = new ROCratePayload();
    this.roCratePreview = new CustomPreview();
    this.metadataContext = new ROCrateMetadataContext(Collections.singletonList(DEFAULT_CONTEXT));
  }

  public ROCrate(ROCrateBuilder roCrateBuilder) {
    this.roCratePayload = roCrateBuilder.payload;
    this.metadataContext = roCrateBuilder.metadataContext;
    this.roCratePreview = roCrateBuilder.preview;
    this.rootDataEntity = roCrateBuilder.rootDataEntity;
    this.jsonDescriptor = roCrateBuilder.jsonDescriptor;
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
    // firstly search the root data entity and the descriptor
    this.roCratePayload.removeAllOccurrencesOf(entityId);
    // also remove in the root data entity and the descriptor
    this.rootDataEntity.setProperties(JsonHelpFunctions.removeFieldsWith(entityId, this.rootDataEntity.getProperties()));
    this.jsonDescriptor.setProperties(JsonHelpFunctions.removeFieldsWith(entityId, this.jsonDescriptor.getProperties()));
  }

  static final public class ROCrateBuilder {

    IROCratePayload payload;
    IROCratePreview preview;
    IROCrateMetadataContext metadataContext;
    ContextualEntity license;
    RootDataEntity rootDataEntity;
    DataEntity jsonDescriptor;

    public ROCrateBuilder(String name, String description) {
      this.payload = new ROCratePayload();
      this.preview = new CustomPreview();
      this.metadataContext = new ROCrateMetadataContext(Collections.singletonList(DEFAULT_CONTEXT));
      rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
          .addProperty("name", name)
          .addProperty("description", description)
          .build();
      jsonDescriptor = new DataEntity.DataEntityBuilder()
          .setId(ID)
          .addType("CreativeWork")
          .addIdProperty("about", "./")
          .addIdProperty("conformsTo", RO_SPEC)
          .build();
    }

    public ROCrateBuilder addDataEntity(DataEntity dataEntity) {
      this.payload.addDataEntity(dataEntity);
      this.rootDataEntity.addToHasPart(dataEntity.getId());
      return this;
    }

    public ROCrateBuilder addContextualEntity(ContextualEntity contextualEntity) {
      this.payload.addContextualEntity(contextualEntity);
      return this;
    }

    public ROCrateBuilder setLicense(ContextualEntity license) {
      this.license = license;
      this.rootDataEntity.addIdProperty("license", license.getId());
      return this;
    }

    public ROCrateBuilder setContext(IROCrateMetadataContext context) {
      this.metadataContext = context;
      return this;
    }

    public ROCrateBuilder addURLToContext(java.lang.String url) {
      this.metadataContext.addToContextFromUrl(url);
      return this;
    }

    public ROCrateBuilder addValuePairToContext(java.lang.String key, java.lang.String value) {
      this.metadataContext.addToContext(key, value);
      return this;
    }

    public ROCrateBuilder setPreview(IROCratePreview preview) {
      this.preview = preview;
      return this;
    }

    public ROCrateBuilder setJSONDescriptor(DataEntity descriptor) {
      this.jsonDescriptor = descriptor;
      return this;
    }

    public ROCrate build() {
      return new ROCrate(this);
    }
  }

}
