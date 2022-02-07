package edu.kit.crate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.context.ROCrateMetadataContext;
import edu.kit.crate.entities.AbstractEntity;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.DataSetEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import edu.kit.crate.objectmapper.MyObjectMapper;
import edu.kit.crate.payload.IROCratePayload;
import edu.kit.crate.payload.ROCratePayload;
import edu.kit.crate.preview.IROCratePreview;
import edu.kit.crate.preview.ROCratePreview;
import java.util.Collections;

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
  public DataEntity getDataEntityById(String id) {
    return this.roCratePayload.getDataEntityById(id);
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
  public void addDataEntity(DataEntity entity) {
    this.roCratePayload.addDataEntity(entity);
  }

  @Override
  public void addContextualEntity(ContextualEntity entity) {
    this.roCratePayload.addContextualEntity(entity);
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
      this.preview = new ROCratePreview();
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
      this.rootDataEntity.addIdProperty("license", license.getId());;
      return this;
    }

    public ROCrateBuilder setContext(IROCrateMetadataContext context) {
      this.metadataContext = context;
      return this;
    }

    public ROCrateBuilder addURLToContext(String url) {
      this.metadataContext.addToContextFromUrl(url);
      return this;
    }

    public ROCrateBuilder addValuePairToContext(String key, String value) {
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
