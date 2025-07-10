package org.camunda.community.migration.converter.expression;

import java.util.Objects;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;

public class ExpressionTransformationResultMessageFactory {
  public static Message getMessage(
      ExpressionTransformationResult transformationResult, String link) {
    // no transformation has happened (because the expression is not an expression)
    if (Objects.equals(transformationResult.result(), transformationResult.juelExpression())) {
      return MessageFactory.noExpressionTransformation();
    }
    // check for execution reference

    if (transformationResult.hasExecutionOnly()) {
      return MessageFactory.expressionExecutionNotAvailable(
          transformationResult.context(),
          transformationResult.juelExpression(),
          transformationResult.result(),
          link);

    } else
    // check for method invocation
    if (transformationResult.hasMethodInvocation()) {
      return MessageFactory.expressionMethodNotPossible(
          transformationResult.context(),
          transformationResult.juelExpression(),
          transformationResult.result(),
          link);
    } else {
      // if all is good, just give the default message
      return MessageFactory.expression(
          transformationResult.context(),
          transformationResult.juelExpression(),
          transformationResult.result(),
          link);
    }
  }
}
