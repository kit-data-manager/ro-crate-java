package edu.kit.datamanager.ro_crate.entities.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.serializers.HasPartSerializer;

import java.util.HashSet;
import java.util.Set;

/**
 * A helping class for the creating of Data entities of type Dataset.
 *
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntity extends DataEntity {

    public static final String TYPE = "Dataset";

    /**
     * The hasPart property is used to indicate that the described resource is a
     * composite resource, and to point to the parts that it includes.
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
        this.hasPart = entityBuilder.hasPart;
        this.addType(TYPE);
    }

    public void removeFromHasPart(String str) {
        this.hasPart.remove(str);
    }

    public void addToHasPart(String id) {
        this.hasPart.add(id);
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
            this.hasPart.add(hasPart.asText());
            hasPart.valueStream().forEach(
                    value -> this.hasPart.add(value.asText())
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
