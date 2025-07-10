package org.camunda.community.migration.converter.visitor.impl.attribute;

import static org.camunda.community.migration.converter.expression.ExpressionTransformer.*;
import static org.camunda.community.migration.converter.message.MessageFactory.*;

import java.util.List;
import java.util.function.Consumer;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.ServiceTaskConvertible;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.visitor.AbstractDelegateImplementationVisitor;

public class ExpressionVisitor extends AbstractDelegateImplementationVisitor {
  @Override
  public String attributeLocalName() {
    return "expression";
  }

  @Override
  protected String extractJobType(DomElementVisitorContext context, String attribute) {
    return transformToJobType(attribute).result();
  }

  @Override
  protected List<Consumer<ServiceTaskConvertible>> additionalConversions(
      DomElementVisitorContext context, String attribute) {
    return List.of();
  }

  @Override
  protected Message returnMessage(DomElementVisitorContext context, String attribute) {
    return delegateExpressionAsJobType(extractJobType(context, attribute), attribute);
  }
}
