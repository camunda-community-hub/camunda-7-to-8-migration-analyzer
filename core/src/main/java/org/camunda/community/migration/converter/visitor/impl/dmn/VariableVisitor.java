package org.camunda.community.migration.converter.visitor.impl.dmn;

import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.Convertible;
import org.camunda.community.migration.converter.convertible.VariableConvertible;
import org.camunda.community.migration.converter.visitor.AbstractDecisionElementVisitor;

public class VariableVisitor extends AbstractDecisionElementVisitor {
  @Override
  protected Convertible createConvertible(DomElementVisitorContext context) {
    return new VariableConvertible();
  }

  @Override
  public String localName() {
    return "variable";
  }
}
