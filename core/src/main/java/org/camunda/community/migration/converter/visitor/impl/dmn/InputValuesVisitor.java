package org.camunda.community.migration.converter.visitor.impl.dmn;

import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.Convertible;
import org.camunda.community.migration.converter.convertible.InputValuesConvertible;
import org.camunda.community.migration.converter.visitor.AbstractDecisionElementVisitor;

public class InputValuesVisitor extends AbstractDecisionElementVisitor {
  @Override
  protected Convertible createConvertible(DomElementVisitorContext context) {
    return new InputValuesConvertible();
  }

  @Override
  public String localName() {
    return "inputValues";
  }
}
