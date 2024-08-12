package edu.kit.datamanager.ro_crate.entities.contextual;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class helps to generate a detailing provenance of entities.
 * 
 * @author sabrinechelbi
 */
public class ActionEntity extends ContextualEntity {

    public ActionEntity(AbstractActionEntityBuilder<?> entityBuilder) {
        super(entityBuilder);
        this.addType(entityBuilder.type.getName());
        this.addProperty("name", entityBuilder.name);
        this.addProperty("description", entityBuilder.description);

        this.addDateTimeProperty("startTime", entityBuilder.startTime);
        this.addDateTimeProperty("endTime", entityBuilder.endTime);

        this.addIdProperty("agent", entityBuilder.agent);
        this.addIdListProperties("instrument", entityBuilder.instruments);
        this.addIdListProperties("result", entityBuilder.results);
        this.addIdListProperties("object", entityBuilder.objects);
    }

    abstract static class AbstractActionEntityBuilder<T extends AbstractActionEntityBuilder<T>>
            extends AbstractContextualEntityBuilder<T> {

        protected ActionTypeEnum type;
        protected String description;
        protected String name;
        protected String startTime;
        protected String endTime;
        protected String agent;

        protected Collection<String> objects = new HashSet<>();
        protected Collection<String> results = new HashSet<>();
        protected Collection<String> instruments = new HashSet<>();

        protected AbstractActionEntityBuilder(ActionTypeEnum type) {
            this.type = type;
        }

        public T setDescription(String description) {
            this.description = description;
            return self();
        }

        public T setName(String name) {
            this.name = name;
            return self();
        }

        public T setStartTime(String startTime) {
            this.startTime = startTime;
            return self();
        }

        public T setEndTime(String endTime) {
            this.endTime = endTime;
            return self();
        }

        public T setAgent(String agent) {
            this.agent = agent;
            return self();
        }

        /**
         * Same as calling {@link #addObject(String)} with each element of the collection.
         * 
         * @param objects the objects to add to this ActionEntity
         * @return this builder
         */
        public T addObjects(Collection<String> objects) {
            this.objects.addAll(objects);
            return self();
        }

        /**
         * Adds a object to the collection of objects of this ActionEntity.
         * 
         * @param object the object to add to this ActionEntity. Duplicates will be
         *               ignored/removed.
         * @return this builder
         */
        public T addObject(String object) {
            this.objects.add(object);
            return self();
        }

        /**
         * Same as calling {@link #addResult(String)} with each element of the collection.
         * 
         * @param results the results to add to this ActionEntity
         * @return this builder
         */
        public T addResults(Collection<String> results) {
            this.results.addAll(results);
            return self();
        }

        /**
         * Adds a result to the collection of results of this ActionEntity.
         * 
         * @param result the result to add to this ActionEntity. Duplicates will be ignored/removed.
         * @return this builder
         */
        public T addResult(String result) {
            this.results.add(result);
            return self();
        }

        /**
         * Same as calling {@link #addInstrument(String)} with each element of the collection.
         * 
         * @param results the results to add to this ActionEntity
         * @return this builder
         */
        public T addInstruments(Collection<String> instruments) {
            this.instruments.addAll(instruments);
            return self();
        }

        /**
         * Adds a instrument to the collection of instruments of this ActionEntity.
         * 
         * @param instrument the instrument to add to this ActionEntity. Duplicates will be ignored/removed.
         * @return this builder
         */
        public T addInstrument(String instrument) {
            this.instruments.add(instrument);
            return self();
        }

        @Override
        public abstract ActionEntity build();
    }

    public static final class ActionEntityBuilder
            extends AbstractActionEntityBuilder<ActionEntityBuilder> {

        public ActionEntityBuilder(ActionTypeEnum type) {
            super(type);
        }

        @Override
        public ActionEntityBuilder self() {
            return this;
        }

        @Override
        public ActionEntity build() {
            return new ActionEntity(this);
        }
    }
}
