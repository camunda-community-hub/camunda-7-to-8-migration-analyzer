package org.camunda.community.migration.converter.convertible;

import org.camunda.community.migration.converter.convertible.UserTaskConvertible.ZeebeFormDefinition;

public class StartEventConvertible extends AbstractCatchEventConvertible
    implements FormDefinitionConvertible {
  private final ZeebeFormDefinition zeebeFormDefinition = new ZeebeFormDefinition();

  @Override
  public ZeebeFormDefinition getZeebeFormDefinition() {
    return zeebeFormDefinition;
  }

  @Override
  public boolean isZeebeUserTask() {
    // not relevant, the start form will only have a form id
    return true;
  }
}
