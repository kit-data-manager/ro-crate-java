package edu.kit.crate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.context.ROCrateMetadataContext;
import edu.kit.crate.externalproviders.dataentities.ImportFromDataCite;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.payload.IROCratePayload;
import edu.kit.crate.payload.ROCratePayload;
import edu.kit.crate.special.JsonHelpFunctions;
import edu.kit.crate.preview.IROCratePreview;
import edu.kit.crate.validation.JsonSchemaValidation;
import edu.kit.crate.validation.Validator;

import java.io.File;
import java.util.ArrayList;
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

  private List<File> untrackedFiles;

  @Override
  public IROCratePreview getPreview() {
    return this.roCratePreview;
  }

  public void setRoCratePreview(IROCratePreview preview) {
    this.roCratePreview = preview;
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
    this.metadataContext = new ROCrateMetadataContext(Collections.singletonList(DEFAULT_CONTEXT));
    this.untrackedFiles = new ArrayList<>();
  }

  public ROCrate(ROCrateBuilder roCrateBuilder) {
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
    JsonHelpFunctions.removeFieldsWith(entityId, this.rootDataEntity.getProperties());
    JsonHelpFunctions.removeFieldsWith(entityId, this.jsonDescriptor.getProperties());
  }

  @Override
  public void setUntrackedFiles(List<File> files) {
    this.untrackedFiles = files;
  }

  @Override
  public void addFromDataCiteSchema(String locationURL) {
    ImportFromDataCite.addDataCiteResource(locationURL, this);
  }

  @Override
  public List<File> getUntrackedFiles() {
    return this.untrackedFiles;
  }

  static final public class ROCrateBuilder {

    IROCratePayload payload;
    IROCratePreview preview;
    IROCrateMetadataContext metadataContext;
    ContextualEntity license;
    RootDataEntity rootDataEntity;
    DataEntity jsonDescriptor;
    List<File> untrackedFiles;

    public ROCrateBuilder(String name, String description) {
      this.payload = new ROCratePayload();
      this.untrackedFiles = new ArrayList<>();
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

    public ROCrateBuilder addUntrackedFile(File file) {
      this.untrackedFiles.add(file);
      return this;
    }

    public ROCrate build() {
      return new ROCrate(this);
    }
  }

}
