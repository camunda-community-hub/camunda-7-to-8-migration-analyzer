package org.camunda.community.migration.converter;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.camunda.community.migration.converter.expression.ExpressionTransformationResult;
import org.camunda.community.migration.converter.expression.ExpressionTransformer;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class ExpressionTransformerTest {

  private static ExpressionTestBuilder expressionToFeel(String expression) {
    return new ExpressionTestBuilder(expression, TransformationType.feel);
  }

  private static ExpressionTestBuilder expressionToJobType(String expression) {
    return new ExpressionTestBuilder(expression, TransformationType.jobType);
  }

  @TestFactory
  public Stream<DynamicContainer> shouldResolveExpression() {
    return Stream.of(
            expressionToFeel("").isMappedTo("=null"),
            expressionToFeel("${someVariable}").isMappedTo("=someVariable"),
            expressionToFeel("someStaticValue").isMappedTo("someStaticValue"),
            expressionToFeel("${var.innerField}").isMappedTo("=var.innerField"),
            expressionToFeel("hello-${World}").isMappedTo("=\"hello-\" + World"),
            expressionToFeel("#{x}").isMappedTo("=x"),
            expressionToFeel("${x}").isMappedTo("=x"),
            expressionToFeel("#{x>5}").isMappedTo("=x>5"),
            expressionToFeel("#{x gt 5}").isMappedTo("=x > 5"),
            expressionToFeel("#{x < 5}").isMappedTo("=x < 5"),
            expressionToFeel("#{x lt 5}").isMappedTo("=x < 5"),
            expressionToFeel("#{x==5}").isMappedTo("=x=5"),
            expressionToFeel("#{x!=5}").isMappedTo("=x!=5"),
            expressionToFeel("#{x eq 5}").isMappedTo("=x = 5"),
            expressionToFeel("#{x ne 5}").isMappedTo("=x != 5"),
            expressionToFeel("#{x == \"test\"}").isMappedTo("=x = \"test\""),
            expressionToFeel("#{true}").isMappedTo("=true"),
            expressionToFeel("${false}").isMappedTo("=false"),
            expressionToFeel("#{!x}").isMappedTo("=not(x)"),
            expressionToFeel("${!x and y}").isMappedTo("=not(x) and y"),
            expressionToFeel("${!(x and y)}").isMappedTo("=not(x and y)"),
            expressionToFeel("#{!true}").isMappedTo("=not(true)"),
            expressionToFeel("#{not(x>5)}").isMappedTo("=not(x>5)"),
            expressionToFeel("${not x}").isMappedTo("=not(x)"),
            expressionToFeel("#{x && y}").isMappedTo("=x and y"),
            expressionToFeel("#{x and y}").isMappedTo("=x and y"),
            expressionToFeel("#{x || y}").isMappedTo("=x or y"),
            expressionToFeel("#{x or y}").isMappedTo("=x or y"),
            expressionToFeel("#{customer.name}").isMappedTo("=customer.name"),
            expressionToFeel("#{customer.address[\"street\"]}")
                .isMappedTo("=customer.address.street"),
            expressionToFeel("#{customer.orders[1]}").isMappedTo("=customer.orders[2]"),
            expressionToFeel("${not empty x}").isMappedTo("=not(x=null)"),
            expressionToFeel("${empty donut}").isMappedTo("=donut=null"),
            expressionToFeel("${!empty donut}").isMappedTo("=not(donut=null)"),
            expressionToFeel("${empty donut || coffee}").isMappedTo("=donut=null or coffee"),
            expressionToFeel("${not empty donut || coffee}")
                .isMappedTo("=not(donut=null) or coffee"),
            expressionToFeel("${not(empty donut || coffee)}")
                .isMappedTo("=not(donut=null or coffee)"),
            expressionToFeel("${execution.getVariable(\"a\")}").isMappedTo("=a"),
            expressionToFeel("${execution.getProcessInstanceId()}").hasUsedExecution(true),
            expressionToFeel("${myexecutionContext.isSpecial()}").hasUsedExecution(false),
            expressionToFeel("${var.getSomething()}").hasMethodInvocation(true),
            expressionToFeel("${!dauerbuchungVoat21Ids.isEmpty()}").hasMethodInvocation(true),
            expressionToFeel("${!dauerbuchungVoat21Ids.contains(\"someText\")}")
                .hasMethodInvocation(true),
            expressionToFeel("${input > 5.5}").hasMethodInvocation(false),
            expressionToFeel("${input != ''}").isMappedTo("=input != \"\""),
            expressionToFeel("${input != 'what the F***'}")
                .isMappedTo("=input != \"what the F***\""),
            expressionToFeel("${array[index]}").isMappedTo("=array[index]"),
            expressionToJobType("${myDelegate}").isMappedTo("myDelegate"),
            expressionToJobType("${myDelegate.doSomething()}")
                .isMappedTo("myDelegateDoSomething")
                .hasMethodInvocation(true),
            expressionToJobType("${myDelegate.doSomething.else}")
                .isMappedTo("myDelegateDoSomethingElse"))
        .map(
            data ->
                DynamicContainer.dynamicContainer(
                    "Expression to "
                        + data.getTransformationType()
                        + ": "
                        + data.getResult().juelExpression(),
                    data.getTests()));
  }

  private enum TransformationType {
    feel,
    jobType
  }

  private static class ExpressionTestBuilder {
    private final TransformationType transformationType;
    private final ExpressionTransformationResult result;
    private final List<DynamicTest> tests = new ArrayList<>();

    public ExpressionTestBuilder(String expression, TransformationType type) {
      this.transformationType = type;
      this.result =
          switch (type) {
            case feel -> ExpressionTransformer.transformToFeel("Test", expression);
            case jobType -> ExpressionTransformer.transformToJobType(expression);
          };
    }

    public TransformationType getTransformationType() {
      return transformationType;
    }

    public ExpressionTransformationResult getResult() {
      return result;
    }

    public List<DynamicTest> getTests() {
      return tests;
    }

    public ExpressionTestBuilder isMappedTo(String expectedResult) {
      tests.add(
          DynamicTest.dynamicTest(
              "Expect Result: '" + expectedResult + "'",
              () -> assertThat(result.result()).isEqualTo(expectedResult)));
      return this;
    }

    public ExpressionTestBuilder hasMethodInvocation(boolean expected) {
      tests.add(
          DynamicTest.dynamicTest(
              String.format("Expect %s method invocation", expected ? "a" : "no"),
              () -> assertThat(result.hasMethodInvocation()).isEqualTo(expected)));
      return this;
    }

    public ExpressionTestBuilder hasUsedExecution(boolean expected) {
      tests.add(
          DynamicTest.dynamicTest(
              String.format("Expect %s execution used", expected ? "a" : "no"),
              () -> assertThat(result.hasExecutionOnly()).isEqualTo(expected)));
      return this;
    }
  }
}
