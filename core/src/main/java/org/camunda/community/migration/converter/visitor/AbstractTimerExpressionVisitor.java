package org.camunda.community.migration.converter.visitor;

import static org.camunda.community.migration.converter.NamespaceUri.*;

import java.util.Objects;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.AbstractCatchEventConvertible;
import org.camunda.community.migration.converter.expression.ExpressionTransformationResult;
import org.camunda.community.migration.converter.expression.ExpressionTransformer;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;

public abstract class AbstractTimerExpressionVisitor extends AbstractBpmnElementVisitor {

  @Override
  protected final void visitBpmnElement(DomElementVisitorContext context) {
    if (!isTimeoutListener(context)) {
      ExpressionTransformationResult transformationResult = transformTimer(context);
      context.addConversion(
          AbstractCatchEventConvertible.class,
          con -> setNewExpression(con, transformationResult.result()));
      if (!Objects.equals(transformationResult.result(), transformationResult.juelExpression())) {
        context.addMessage(
            MessageFactory.timerExpressionMapped(
                transformationResult.juelExpression(), transformationResult.result()));
      }
    }
  }

  private boolean isTimeoutListener(DomElementVisitorContext context) {
    return isOnBpmnElement(context, CAMUNDA, "taskListener");
  }

  private ExpressionTransformationResult transformTimer(DomElementVisitorContext context) {
    return ExpressionTransformer.transformToFeel("Timer", context.getElement().getTextContent());
  }

  protected abstract void setNewExpression(
      AbstractCatchEventConvertible convertible, String newExpression);

  protected boolean isStartEvent(DomElement element) {
    return element.getParentElement().getParentElement().getLocalName().equals("startEvent")
        && !isEventSubprocess(element);
  }

  protected boolean isNonInterruptingIntermediate(DomElement element) {
    String cancelActivity =
        element.getParentElement().getParentElement().getAttribute(BPMN, "cancelActivity");
    return Boolean.FALSE.toString().equals(cancelActivity);
  }

  protected boolean isNonInterruptingStart(DomElement element) {
    String isInterrupting =
        element.getParentElement().getParentElement().getAttribute(BPMN, "isInterrupting");
    return Boolean.FALSE.toString().equals(isInterrupting);
  }

  protected boolean isIntermediateEvent(DomElement element) {
    return element
        .getParentElement()
        .getParentElement()
        .getLocalName()
        .equals("intermediateCatchEvent");
  }

  protected boolean isBoundaryEvent(DomElement element) {
    return element.getParentElement().getParentElement().getLocalName().equals("boundaryEvent");
  }

  protected boolean isEventSubprocess(DomElement element) {
    return element.getParentElement().getParentElement().getLocalName().equals("startEvent")
        && Boolean.parseBoolean(
            element
                .getParentElement()
                .getParentElement()
                .getParentElement()
                .getAttribute("triggeredByEvent"));
  }

  @Override
  protected Message cannotBeConvertedMessage(DomElementVisitorContext context) {
    return MessageFactory.timerExpressionNotSupported(
        elementNameForMessage(context.getElement()),
        transformTimer(context).result(),
        elementNameForMessage(context.getElement().getParentElement().getParentElement()),
        context.getProperties().getPlatformVersion());
  }
}
