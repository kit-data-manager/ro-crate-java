package edu.kit.datamanager.ro_crate.entities.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A helping class for the creation of workflow data entities.
 */
public class WorkflowEntity extends FileEntity {

  private static final List<String> TYPES = List.of(
      new String[]{"SoftwareSourceCode", "ComputationalWorkflow"});

  /**
   * Constructor for creating the workflow entity from a builder.
   *
   * @param entityBuilder the builder from which the entity should be created.
   */
  public WorkflowEntity(AbstractWorkflowEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    for (String str : TYPES) {
      this.addType(str);
    }
    this.addIdListProperties("input", entityBuilder.input);
    this.addIdListProperties("output", entityBuilder.output);
  }

  abstract static class AbstractWorkflowEntityBuilder<T
      extends AbstractWorkflowEntityBuilder<T>> extends AbstractFileEntityBuilder<T> {

    List<String> input = new ArrayList<>();
    List<String> output = new ArrayList<>();

    public T addInput(String input) {
      this.input.add(input);
      return self();
    }

    public T addOutput(String output) {
      this.output.add(output);
      return self();
    }

    @Override
    public abstract WorkflowEntity build();
  }

  /**
   * A builder for easier creation of workflow entities.
   */
  public static final class WorkflowEntityBuilder extends
      AbstractWorkflowEntityBuilder<WorkflowEntityBuilder> {

    @Override
    public WorkflowEntityBuilder self() {
      return this;
    }

    @Override
    public WorkflowEntity build() {
      return new WorkflowEntity(this);
    }
  }
}
