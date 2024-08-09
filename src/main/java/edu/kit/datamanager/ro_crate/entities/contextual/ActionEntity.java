package edu.kit.datamanager.ro_crate.entities.contextual;

import java.util.ArrayList;
import java.util.List;

/**
 * This class helps to generate a detailing provenance of entities.
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

        private ActionTypeEnum type;
        private String description;
        private String name;
        private String startTime;
        private String endTime;
        private String agent;

        List<String> objects = new ArrayList<>();
        List<String> results = new ArrayList<>();
        List<String> instruments = new ArrayList<>();

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

        public T addObjects(List<String> objects) {
            this.objects.addAll(objects);
            return self();
        }

        public T addObject(String object) {
            this.objects.add(object);
            return self();
        }

        public T addResults(List<String> results) {
            this.results.addAll(results);
            return self();
        }
        public T addResult(String result) {
            this.results.add(result);
            return self();
        }

        public T addInstruments(List<String> instruments) {
            this.instruments.addAll(instruments);
            return self();
        }

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
