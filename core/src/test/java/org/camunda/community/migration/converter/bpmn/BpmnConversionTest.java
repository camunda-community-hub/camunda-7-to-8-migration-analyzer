package org.camunda.community.migration.converter.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.applyProperties;
import static org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.asXml;
import static org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.extractMessageNodes;
import static org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.extractSnippet;
import static org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.wrapSnippetInProcess;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.transform.stream.StreamSource;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.community.migration.converter.ConverterProperties;
import org.camunda.community.migration.converter.ConverterPropertiesFactory;
import org.camunda.community.migration.converter.DefaultConverterProperties;
import org.camunda.community.migration.converter.DiagramConverter;
import org.camunda.community.migration.converter.DiagramConverterFactory;
import org.camunda.community.migration.converter.bpmn.BpmnTestcaseLoader.BpmnConversionCase;
import org.camunda.community.migration.converter.bpmn.BpmnTestcaseUtils.MessageNode;
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

  //  public static void main(String[] args) {
  //    BpmnConversionCase testCase = new BpmnConversionCase();
  //    testCase.givenBpmn =
  //        """
  //        """;
  //    String actualBpmn = new BpmnConversionTest().check(testCase);
  //    logTestCase(testCase, actualBpmn);
  //  }

  public String check(BpmnConversionCase testCase) {
    DefaultConverterProperties myProps = new DefaultConverterProperties();
    myProps.setAppendDocumentation(false);
    applyProperties(myProps, testCase.properties);
    ConverterProperties properties = ConverterPropertiesFactory.getInstance().merge(myProps);

    BpmnModelInstance modelInstance = wrapSnippetInProcess(testCase.givenBpmn());

    DiagramConverter converter = DiagramConverterFactory.getInstance().get();
    converter.convert(modelInstance, properties);

    return extractSnippet(modelInstance);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("loadConversionCases")
  void testBpmnFromYaml(BpmnConversionCase testCase) throws Exception {

    String actualBpmn = check(testCase);

    logTestCase(testCase, actualBpmn);

    XmlAssert.assertThat(asXml(actualBpmn))
        .and(asXml(testCase.expectedBpmn()))
        .withNodeFilter(noConversionAndEmptyExtensionElements())
        .ignoreWhitespace()
        .normalizeWhitespace()
        .withNodeMatcher(
            new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)) // ignore sibling order
        .areSimilar();

    if (testCase.expectedMessages().isEmpty()) {
      assertNoConversionMessages(asXml(actualBpmn));
    } else {
      assertConversionMessagesEqual(asXml(testCase.expectedMessages()), asXml(actualBpmn));
    }
  }

  public static Predicate<Node> noConversionAndEmptyExtensionElements() {
    return node -> {
      if (node.getNamespaceURI() != null && node.getNamespaceURI().contains("conversion")) {
        return false;
      }

      // ignore <extensionElements> if they are empty once all conversion messages are removed:
      if ("extensionElements".equals(node.getLocalName())) {
        // Check if it contains non-conversion elements
        Node child = node.getFirstChild();
        while (child != null) {
          if (child.getNamespaceURI() == null || !child.getNamespaceURI().contains("conversion")) {
            return true; // keep if it has real content
          }
          child = child.getNextSibling();
        }
        return false; // empty or only conversion messages → ignore
      }

      return true; // Keep all other nodes
    };
  }

  public static void assertConversionMessagesEqual(String expectedXml, String actualXml)
      throws Exception {
    Set<MessageNode> expected = extractMessageNodes(expectedXml);
    Set<MessageNode> actual = extractMessageNodes(actualXml);

    if (!expected.equals(actual)) {
      Set<MessageNode> missing = new HashSet<>(expected);
      missing.removeAll(actual);
      Set<MessageNode> unexpected = new HashSet<>(actual);
      unexpected.removeAll(expected);

      StringBuilder sb = new StringBuilder();
      if (!missing.isEmpty()) {
        sb.append("Missing expected messages:\n");
        missing.forEach(msg -> sb.append("- ").append(msg).append("\n"));
      }
      if (!unexpected.isEmpty()) {
        sb.append("Unexpected actual messages:\n");
        unexpected.forEach(msg -> sb.append("- ").append(msg).append("\n"));
      }

      fail(sb.toString());
    }
  }

  static Stream<BpmnConversionCase> loadConversionCases() throws IOException {
    return BpmnTestcaseLoader.loadFromYaml(
        BpmnConversionTest.class.getResourceAsStream("/BPMN_CONVERSION.yaml"))
        .stream();
  }

  public static void assertNoConversionMessages(String bpmnXml) {
    Document doc = Convert.toDocument(new StreamSource(new StringReader(bpmnXml)));
    NodeList messages =
        doc.getElementsByTagNameNS("http://camunda.org/schema/conversion/1.0", "message");
    assertThat(messages.getLength()).as("Expected no <conversion:message> elements").isEqualTo(0);
  }

  public static Predicate<Node> onlyConversionMessages() {
    return node -> {
      // Include all children of conversion:message nodes
      Node parent = node.getParentNode();
      boolean isMessage =
          node.getNodeType() == Node.ELEMENT_NODE
              && node.getNamespaceURI() != null
              && node.getNamespaceURI().contains("conversion")
              && node.getLocalName().contains("message");

      boolean isChildOfMessage =
          parent != null
              && parent.getNamespaceURI() != null
              && parent.getNamespaceURI().contains("conversion")
              && parent.getLocalName().contains("message");

      return isMessage || isChildOfMessage;
    };
  }

  public static void logTestCase(BpmnConversionCase testCase, String actualBpmn) {
    StringBuilder sb = new StringBuilder();

    sb.append("\n==== BPMN Conversion Test: ").append(testCase.name()).append(" ====\n");

    if (testCase.description() != null && !testCase.description().isBlank()) {
      sb.append(testCase.description()).append("\n");
    }

    sb.append("\n### Given BPMN\n").append(testCase.givenBpmn()).append("\n");

    sb.append("\n### Expected BPMN Snippet\n").append(testCase.expectedBpmn()).append("\n");

    sb.append("\n### Actual BPMN Snippet\n").append(actualBpmn).append("\n");

    if (testCase.expectedMessages() != null && !testCase.expectedMessages().isBlank()) {
      sb.append("\n### Expected Conversion Messages\n")
          .append(testCase.expectedMessages())
          .append("\n");
    }

    sb.append("========================================\n");

    LOG.info(sb.toString());
  }
}
