package edu.kit.datamanager.ro_crate;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.context.CrateMetadataContext;
import edu.kit.datamanager.ro_crate.context.RoCrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.JsonDescriptor;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate.externalproviders.dataentities.ImportFromDataCite;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.payload.CratePayload;
import edu.kit.datamanager.ro_crate.payload.RoCratePayload;
import edu.kit.datamanager.ro_crate.preview.CratePreview;
import edu.kit.datamanager.ro_crate.special.CrateVersion;
import edu.kit.datamanager.ro_crate.special.JsonUtilFunctions;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The class that represents a single ROCrate.
 *
 * To build or modify it, use a instance of {@link RoCrateBuilder}. In the case
 * features of RO-Crate DRAFT specifications are needed, refer to
 * {@link BuilderWithDraftFeatures} and its documentation.
 *
 * @author Nikola Tzotchev on 6.2.2022 г.
 * @version 1
 */
public class RoCrate implements Crate {

    private final CratePayload roCratePayload;
    private CrateMetadataContext metadataContext;
    private CratePreview roCratePreview;
    private RootDataEntity rootDataEntity;
    private ContextualEntity jsonDescriptor;

    private Collection<File> untrackedFiles;

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
        this.untrackedFiles = new HashSet<>();
        this.metadataContext = new RoCrateMetadataContext();
        rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
                .build();
        jsonDescriptor = new JsonDescriptor();
    }

    /**
     * A constructor for creating the crate using a Crate builder for easier
     * creation.
     *
     * @param roCrateBuilder the builder to use.
     */
    public RoCrate(RoCrateBuilder roCrateBuilder) {
        this.roCratePayload = roCrateBuilder.payload;
        this.metadataContext = roCrateBuilder.metadataContext;
        this.roCratePreview = roCrateBuilder.preview;
        this.rootDataEntity = roCrateBuilder.rootDataEntity;
        this.jsonDescriptor = roCrateBuilder.descriptorBuilder.build();
        this.untrackedFiles = roCrateBuilder.untrackedFiles;
        Validator defaultValidation = new Validator(new JsonSchemaValidation());
        defaultValidation.validate(this);
    }

    @Override
    public Optional<CrateVersion> getVersion() {
        JsonNode conformsTo = this.jsonDescriptor.getProperty("conformsTo");
        if (conformsTo.isArray()) {
            return StreamSupport.stream(conformsTo.spliterator(), false)
                    .filter(TreeNode::isObject)
                    .map(obj -> obj.path("@id").asText())
                    .map(CrateVersion::fromSpecUri)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } else if (conformsTo.isObject()) {
            return CrateVersion.fromSpecUri(conformsTo.get("@id").asText());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<String> getProfiles() {
        JsonNode conformsTo = this.jsonDescriptor.getProperty("conformsTo");
        if (conformsTo.isArray()) {
            return StreamSupport.stream(conformsTo.spliterator(), false)
                    .filter(TreeNode::isObject)
                    .map(obj -> obj.path("@id").asText())
                    .filter(txt -> !CrateVersion.fromSpecUri(txt).isPresent())
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
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
    public List<ContextualEntity> getAllContextualEntities() {
        return this.roCratePayload.getAllContextualEntities();
    }

    @Override
    public AbstractEntity getEntityById(String id) {
        return this.roCratePayload.getEntityById(id);
    }

    @Override
    public void addDataEntity(DataEntity entity, Boolean toHasPart) {
        this.metadataContext.checkEntity(entity);
        this.roCratePayload.addDataEntity(entity);
        if (Boolean.TRUE.equals(toHasPart)) {
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
    public void setUntrackedFiles(Collection<File> files) {
        this.untrackedFiles = files;
    }

    @Override
    public void deleteValuePairFromContext(String key) {
        this.metadataContext.deleteValuePairFromContext(key);
    }

    @Override
    public void deleteUrlFromContext(String key) {
        this.metadataContext.deleteUrlFromContext(key);
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
    public Collection<File> getUntrackedFiles() {
        return this.untrackedFiles;
    }

    /**
     * The inner class builder for the easier creation of a ROCrate.
     */
    public static class RoCrateBuilder {

        private static final String PROPERTY_DESCRIPTION = "description";

        CratePayload payload;
        CratePreview preview;
        CrateMetadataContext metadataContext;
        ContextualEntity license;
        RootDataEntity rootDataEntity;
        Collection<File> untrackedFiles = new HashSet<>();

        JsonDescriptor.Builder descriptorBuilder = new JsonDescriptor.Builder();

        /**
         * The default constructor of a builder.
         *
         * @param name the name of the crate.
         * @param description the description of the crate.
         * @param datePublished the published date of the crate.
         * @param licenseId the license identifier of the crate.
         */
        public RoCrateBuilder(String name, String description, String datePublished, String licenseId) {
            this.payload = new RoCratePayload();
            this.metadataContext = new RoCrateMetadataContext();
            this.rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
                    .addProperty("name", name)
                    .addProperty(PROPERTY_DESCRIPTION, description)
                    .build();
            this.setLicense(licenseId);
            this.addDatePublishedWithExceptions(datePublished);
        }

        /**
         * The default constructor of a builder.
         *
         * @param name the name of the crate.
         * @param description the description of the crate.
         * @param datePublished the published date of the crate.
         * @param license the license entity of the crate.
         */
        public RoCrateBuilder(String name, String description, String datePublished, ContextualEntity license) {
            this.payload = new RoCratePayload();
            this.metadataContext = new RoCrateMetadataContext();
            this.rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
                    .addProperty("name", name)
                    .addProperty(PROPERTY_DESCRIPTION, description)
                    .build();
            this.setLicense(license);
            this.addDatePublishedWithExceptions(datePublished);
        }

        /**
         * A default constructor without any params where the root data entity
         * will be plain.
         */
        public RoCrateBuilder() {
            this.payload = new RoCratePayload();
            this.metadataContext = new RoCrateMetadataContext();
            rootDataEntity = new RootDataEntity.RootDataEntityBuilder()
                    .build();
        }

        /**
         * A constructor with a crate as template.
         *
         * @param crate the crate to copy.
         */
        public RoCrateBuilder(RoCrate crate) {
            this.payload = crate.roCratePayload;
            this.preview = crate.roCratePreview;
            this.metadataContext = crate.metadataContext;
            this.rootDataEntity = crate.rootDataEntity;
            this.untrackedFiles = crate.untrackedFiles;
            this.descriptorBuilder = new JsonDescriptor.Builder(crate);
        }

        public RoCrateBuilder addName(String name) {
            this.rootDataEntity.addProperty("name", name);
            return this;
        }

        public RoCrateBuilder addDescription(String description) {
            this.rootDataEntity.addProperty(PROPERTY_DESCRIPTION, description);
            return this;
        }

        /**
         * Adding a data entity to the crate. The important part here is to also
         * add its id to the RootData Entity hasPart.
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
         * @param license the license to set.
         * @return this builder.
         */
        public RoCrateBuilder setLicense(ContextualEntity license) {
            this.license = license;
            // From our tests, it seems like if we only have the ID for our license, we do
            // not need to add an extra entity.
            if (license.getProperties().size() > 1) {
                this.addContextualEntity(license);
            }
            this.rootDataEntity.addIdProperty("license", license.getId());
            return this;
        }

        /**
         * Setting the license of the crate using only a license identifier.
         *
         * @param licenseId the licenses identifier. Should be a resolveable URI.
         * @return the builder
         */
        public RoCrateBuilder setLicense(String licenseId) {
            ContextualEntity licenseEntity = new ContextualEntity.ContextualEntityBuilder()
                .setId(licenseId)
                .build();
            this.setLicense(licenseEntity);
            return this;
        }

        /**
         * Adds a property with date time format. The property should match the ISO 8601
         * date format.
         * 
         * @param dateValue time string in ISO 8601 format
         * @return this builder
         * @throws IllegalArgumentException if format is not ISO 8601
         */
        public RoCrateBuilder addDatePublishedWithExceptions(String dateValue) throws IllegalArgumentException {
            this.rootDataEntity.addDateTimePropertyWithExceptions("datePublished", dateValue);
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

        /**
         * Returns a crate with the information from this builder.
         */
        public RoCrate build() {
            return new RoCrate(this);
        }
    }

    /**
     * Builder for Crates, supporting features which are not in a final
     * specification yet.
     *
     * NOTE: This will change the specification version of your crate.
     *
     * We only add features we expect to be in the new specification in the end.
     * In case a feature will not make it into the specification, we will mark
     * it as deprecated and remove it in new major versions. If a feature is
     * finalized, it will be added to the stable {@link RoCrateBuilder} and
     * marked as deprecated in this class.
     */
    public static class BuilderWithDraftFeatures extends RoCrateBuilder {

        /**
         * @see RoCrateBuilder#RoCrateBuilder()
         */
        public BuilderWithDraftFeatures() {
            super();
        }

        /**
         * @see RoCrateBuilder#RoCrateBuilder(String, String, String, String)
         */
        public BuilderWithDraftFeatures(String name, String description, String datePublished, String licenseId) {
            super(name, description, datePublished, licenseId);
        }

        /**
         * @see RoCrateBuilder#RoCrateBuilder(String, String, String, ContextualEntity)
         */
        public BuilderWithDraftFeatures(String name, String description, String datePublished, ContextualEntity licenseId) {
            super(name, description, datePublished, licenseId);
        }

        /**
         * @see RoCrateBuilder#RoCrateBuilder(RoCrate)
         */
        public BuilderWithDraftFeatures(RoCrate crate) {
            super(crate);
            this.descriptorBuilder = new JsonDescriptor.Builder(crate);
        }

        /**
         * Indicate this crate also conforms to the given specification, in
         * addition to the version this builder adds.
         *
         * This is helpful for profiles or other specifications the crate
         * conforms to. Can be called multiple times to add more specifications.
         *
         * @param specification a specification or profile this crate conforms
         * to.
         * @return the builder
         */
        public BuilderWithDraftFeatures alsoConformsTo(URI specification) {
            descriptorBuilder
                    .addConformsTo(specification)
                    // usage of a draft feature results in draft version numbers of the crate
                    .setVersion(CrateVersion.LATEST_UNSTABLE);
            return this;
        }
    }
}
