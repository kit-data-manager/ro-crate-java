package edu.kit.crate.entities.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class WorkflowEntity extends FileEntity {

  private static final List<String> TYPES = List.of(
      new String[]{"SoftwareSourceCode", "ComputationalWorkflow"});

  public WorkflowEntity(AWorkflowEntityBuilder<?> entityBuilder) {
    super(entityBuilder);
    for (String str : TYPES) {
      this.addType(str);
    }
    this.addIdListProperties("input", entityBuilder.input);
    this.addIdListProperties("output", entityBuilder.output);
  }

  abstract static class AWorkflowEntityBuilder<T extends AWorkflowEntityBuilder<T>> extends
      AFileEntityBuilder<T> {

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
    abstract public WorkflowEntity build();
  }

  final static public class WorkflowEntityBuilder extends
      AWorkflowEntityBuilder<WorkflowEntityBuilder> {

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
