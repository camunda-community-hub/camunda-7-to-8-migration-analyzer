package org.camunda.community.migration.converter.visitor.impl.attribute;

import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.AbstractActivityConvertible;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;
import org.camunda.community.migration.converter.visitor.AbstractSupportedAttributeVisitor;

public class ModelerTemplateVisitor extends AbstractSupportedAttributeVisitor {
  @Override
  protected Message visitSupportedAttribute(DomElementVisitorContext context, String attribute) {
    context.addConversion(
        AbstractActivityConvertible.class, c -> c.setZeebeModelerTemplate(attribute));
    return MessageFactory.modelerTemplate();
  }

  @Override
  public String attributeLocalName() {
    return "modelerTemplate";
  }
}
