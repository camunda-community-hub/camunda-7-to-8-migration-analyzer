package org.camunda.community.migration.converter.visitor.impl.attribute;

import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.FormDefinitionConvertible;
import org.camunda.community.migration.converter.expression.ExpressionTransformationResult;
import org.camunda.community.migration.converter.expression.ExpressionTransformationResultMessageFactory;
import org.camunda.community.migration.converter.expression.ExpressionTransformer;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.visitor.AbstractSupportedAttributeVisitor;

public class FormRefVisitor extends AbstractSupportedAttributeVisitor {
  @Override
  public String attributeLocalName() {
    return "formRef";
  }

  @Override
  protected Message visitSupportedAttribute(DomElementVisitorContext context, String attribute) {
    ExpressionTransformationResult transformationResult =
        ExpressionTransformer.transform("Form Id", attribute);
    context.addConversion(
        FormDefinitionConvertible.class,
        conversion ->
            conversion
                .getZeebeFormDefinition()
                .setFormId(transformationResult.getFeelExpression()));
    return ExpressionTransformationResultMessageFactory.getMessage(
        transformationResult,
        "https://docs.camunda.io/docs/components/modeler/bpmn/user-tasks/#user-task-forms");
  }
}
