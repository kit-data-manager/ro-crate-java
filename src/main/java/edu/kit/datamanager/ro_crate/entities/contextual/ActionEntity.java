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
        this.addIdListProperties("instrument", entityBuilder.instrument);
        this.addIdListProperties("result", entityBuilder.result);
        this.addIdListProperties("object", entityBuilder.object);
    }

    abstract static class AbstractActionEntityBuilder<T extends AbstractActionEntityBuilder<T>>
            extends AbstractContextualEntityBuilder<T> {

        ActionTypeEnum type;
        String description;
        String name;
        String startTime;
        String endTime;
        String agent;

        List<String> object = new ArrayList<>();
        List<String> result = new ArrayList<>();
        List<String> instrument = new ArrayList<>();

        public T addType(ActionTypeEnum type) {
            this.type = type;
            return self();
        }

        public T addDescription(String description) {
            this.description = description;
            return self();
        }

        public T addName(String name) {
            this.name = name;
            return self();
        }

        public T addStartTime(String startTime) {
            this.startTime = startTime;
            return self();
        }

        public T addEndTime(String endTime) {
            this.endTime = endTime;
            return self();
        }

        public T addAgent(String agent) {
            this.agent = agent;
            return self();
        }

        public T addObject(List<String> object) {
            this.object = object;
            return self();
        }

        public T addResult(List<String> result) {
            this.result = result;
            return self();
        }

        public T addInstrument(List<String> instrument) {
            this.instrument = instrument;
            return self();
        }

        @Override
        public abstract ActionEntity build();
    }

    public static final class ActionEntityBuilder
            extends AbstractActionEntityBuilder<ActionEntityBuilder> {

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
