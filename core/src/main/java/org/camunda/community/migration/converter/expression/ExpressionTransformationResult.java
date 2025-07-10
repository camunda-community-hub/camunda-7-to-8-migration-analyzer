package org.camunda.community.migration.converter.expression;

public record ExpressionTransformationResult(
    String context,
    String juelExpression,
    String result,
    Boolean hasMethodInvocation,
    Boolean hasExecutionOnly) {}
