package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.serializers.HasPartSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A helping class for the creating of Data entities of type Dataset.
 *
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntity extends DataEntity {

    public static final String TYPE = "Dataset";

    /**
     * Points to the parts of this dataset.
     * <p>
     * This will be serialized to and deserialized from the "hasPart" property
     * and exists for convenience to represent the additional capabilities of
     * a DataSetEntity over a normal DataEntity.
     */
    @JsonSerialize(using = HasPartSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Set<String> hasPart;

    /**
     * Constructor for instantiating a Dataset entity from the builder.
     *
     * @param entityBuilder the builder passed as argument.
     */
    public DataSetEntity(AbstractDataSetBuilder<?> entityBuilder) {
        super(entityBuilder);
        this.hasPart = entityBuilder.hasPart.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        this.addType(TYPE);
    }

    public void removeFromHasPart(String str) {
        this.hasPart.remove(str);
    }

    public void addToHasPart(String id) {
        if (id != null && !id.isEmpty()) {
            this.hasPart.add(id);
        }
    }

    /**
     * Check if the hasPart property contains a specific id.
     *
     * @deprecated use {@link #hasPart(String)} instead.
     *
     * @param id the id to check for
     * @return true if the id is present, false otherwise
     */
    @Deprecated(forRemoval = true)
    public boolean hasInHasPart(String id) {
        return this.hasPart.contains(id);
    }

    /**
     * Check if the hasPart property contains a specific id.
     * @param id the id to check for
     * @return true if the id is present, false otherwise
     */
    public boolean hasPart(String id) {
        return this.hasPart.contains(id);
    }

    abstract static class AbstractDataSetBuilder<T extends AbstractDataEntityBuilder<T>> extends
            AbstractDataEntityBuilder<T> {

        Set<String> hasPart;

        public AbstractDataSetBuilder() {
            this.hasPart = new HashSet<>();
        }

        public T setHasPart(Set<String> hasPart) {
            this.hasPart = hasPart;
            return self();
        }

        public T addToHasPart(DataEntity dataEntity) {
            if (dataEntity != null) {
                this.hasPart.add(dataEntity.getId());
                this.relatedItems.add(dataEntity.getId());
            }
            return self();
        }

        public T addToHasPart(String dataEntity) {
            if (dataEntity != null) {
                this.hasPart.add(dataEntity);
                this.relatedItems.add(dataEntity);
            }
            return self();
        }

        @Override
        public T setAllUnsafe(ObjectNode properties) {
            super.setAllUnsafe(properties);
            JsonNode hasPart = properties.path("hasPart");
            String txt = hasPart.asText();
            if (!txt.isBlank()) {
                this.hasPart.add(txt);
            }
            hasPart.valueStream()
                    .map(JsonNode::asText)
                    .filter(value -> !value.isBlank())
                    .forEach(
                            value -> this.hasPart.add(value)
                    );
            return self();
        }

        @Override
        public abstract DataSetEntity build();
    }

    /**
     * Builder for the helping class DataSetEntity.
     */
    public static final class DataSetBuilder extends AbstractDataSetBuilder<DataSetBuilder> {

        @Override
        public DataSetBuilder self() {
            return this;
        }

        @Override
        public DataSetEntity build() {
            return new DataSetEntity(this);
        }
    }
}
