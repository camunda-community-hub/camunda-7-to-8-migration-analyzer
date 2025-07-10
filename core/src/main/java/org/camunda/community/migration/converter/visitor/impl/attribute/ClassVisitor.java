package org.camunda.community.migration.converter.visitor.impl.attribute;

import java.util.List;
import java.util.function.Consumer;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.ServiceTaskConvertible;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;
import org.camunda.community.migration.converter.visitor.AbstractDelegateImplementationVisitor;

public class ClassVisitor extends AbstractDelegateImplementationVisitor {

  @Override
  public String attributeLocalName() {
    return "class";
  }

  @Override
  protected String extractJobType(DomElementVisitorContext context, String attribute) {
    int lastDot = attribute.lastIndexOf('.');
    return decapitalize(attribute.substring(lastDot + 1));
  }

  @Override
  protected List<Consumer<ServiceTaskConvertible>> additionalConversions(
      DomElementVisitorContext context, String attribute) {
    return List.of();
  }

  @Override
  protected Message returnMessage(DomElementVisitorContext context, String attribute) {
    return MessageFactory.delegateExpressionAsJobType(
        extractJobType(context, attribute), attribute);
  }

  private String decapitalize(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }
}
