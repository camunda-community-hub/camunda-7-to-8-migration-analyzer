package org.camunda.community.migration.converter.visitor.impl.dmn;

import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.Convertible;
import org.camunda.community.migration.converter.convertible.OutputValuesConvertible;
import org.camunda.community.migration.converter.visitor.AbstractDecisionElementVisitor;

public class OutputValuesVisitor extends AbstractDecisionElementVisitor {
  @Override
  protected Convertible createConvertible(DomElementVisitorContext context) {
    return new OutputValuesConvertible();
  }

  @Override
  public String localName() {
    return "outputValues";
  }
}
