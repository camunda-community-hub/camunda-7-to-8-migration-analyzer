package org.camunda.community.migration.converter.convertible;

import org.camunda.community.migration.converter.convertible.UserTaskConvertible.ZeebeFormDefinition;

public interface FormDefinitionConvertible extends Convertible {
  ZeebeFormDefinition getZeebeFormDefinition();

  boolean isZeebeUserTask();
}
