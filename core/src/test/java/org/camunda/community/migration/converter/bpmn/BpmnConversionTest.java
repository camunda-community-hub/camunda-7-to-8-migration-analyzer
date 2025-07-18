package org.camunda.community.migration.converter.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.community.migration.converter.bpmn.ModelUtilities.asXml;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import javax.xml.transform.stream.StreamSource;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.community.migration.converter.ConverterProperties;
import org.camunda.community.migration.converter.ConverterPropertiesFactory;
import org.camunda.community.migration.converter.DefaultConverterProperties;
import org.camunda.community.migration.converter.DiagramConverter;
import org.camunda.community.migration.converter.DiagramConverterFactory;
import org.camunda.community.migration.converter.bpmn.BpmnConversionCaseLoader.BpmnConversionCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.util.Convert;
import org.xmlunit.util.Predicate;

public class BpmnConversionTest {
  private static final Logger LOG = LoggerFactory.getLogger(BpmnConversionTest.class);
  
  public String check(BpmnConversionCase testCase) {
    DefaultConverterProperties myProps = new DefaultConverterProperties();
    myProps.setAppendDocumentation(false);
    ConverterProperties properties = ConverterPropertiesFactory.getInstance().merge(myProps);

    BpmnModelInstance modelInstance = ModelUtilities.wrapSnippetInProcess(testCase.givenBpmn());

    DiagramConverter converter = DiagramConverterFactory.getInstance().get();
    converter.convert(modelInstance, properties);

    return ModelUtilities.extractSnippet(modelInstance, testCase.givenBpmn());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("loadConversionCases")
  void testBpmnFromYaml(BpmnConversionCase testCase) {

    String actualBpmn = check(testCase);

    logTestCase(testCase, actualBpmn);

    XmlAssert.assertThat(asXml(actualBpmn))
        .and(asXml(testCase.expectedBpmn()))
        .withNodeFilter(noConversion())
        .ignoreWhitespace()
        .normalizeWhitespace()
        .withNodeMatcher(
            new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)) // ignore sibling order
        .areSimilar();

    if (testCase.expectedMessages().isEmpty()) {
      assertNoConversionMessages(asXml(actualBpmn));
    } else {
      XmlAssert.assertThat(asXml(actualBpmn))
          .and(asXml(testCase.expectedMessages()))
          .withNodeFilter(onlyConversionMessages())
          .ignoreWhitespace()
          .normalizeWhitespace()
          .withNodeMatcher(
              new DefaultNodeMatcher(
                  ElementSelectors.byNameAndAllAttributes)) // ignore sibling order
          .withAttributeFilter(attr -> !"link".equals(attr.getName()))
          .areSimilar();
    }
  }

  static Stream<BpmnConversionCase> loadConversionCases() throws IOException {
    return BpmnConversionCaseLoader.loadFromYaml(
        BpmnConversionTest.class.getResourceAsStream("/BPMN_CONVERSION.yaml"))
        .stream();
  }

  public static void assertNoConversionMessages(String bpmnXml) {
    Document doc = Convert.toDocument(new StreamSource(new StringReader(bpmnXml)));
    NodeList messages =
        doc.getElementsByTagNameNS("http://camunda.org/schema/conversion/1.0", "message");
    assertThat(messages.getLength()).as("Expected no <conversion:message> elements").isEqualTo(0);
  }

  /** Ignore all nodes in 'conversion' namespace */
  public static Predicate<Node> noConversion() {
    return node -> {
      if (node.getNamespaceURI() != null && node.getNamespaceURI().contains("conversion")) {
        return false;
      }
      return true;
    };
  }

  /** Ignore all nodes in 'conversion' namespace */
  public static Predicate<Node> onlyConversionMessages() {
    return node -> {
      if (node.getNamespaceURI() != null
          && node.getNamespaceURI().contains("conversion")
          && node.getLocalName().contains("message")) {
        return true;
      }
      return false;
    };
  }

  public static void logTestCase(BpmnConversionCase testCase, String actualBpmn) {
    StringBuilder sb = new StringBuilder();

    sb.append("\n==== BPMN Conversion Test: ").append(testCase.name()).append(" ====\n");
    
    if (testCase.description() != null && !testCase.description().isBlank()) {
      sb.append(testCase.description()).append("\n");
    }

    sb.append("\n### Given BPMN\n")
      .append(testCase.givenBpmn()).append("\n");

    sb.append("\n### Expected BPMN Snippet\n")
      .append(testCase.expectedBpmn()).append("\n");

    sb.append("\n### Actual BPMN Snippet\n")
      .append(actualBpmn).append("\n");

    if (testCase.expectedMessages() != null && !testCase.expectedMessages().isBlank()) {
      sb.append("\n### Expected Conversion Messages\n")
        .append(testCase.expectedMessages()).append("\n");
    }

    sb.append("========================================\n");

    LOG.info(sb.toString());
  }

}
