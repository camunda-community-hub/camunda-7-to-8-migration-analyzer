package org.camunda.community.migration.converter.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.community.migration.converter.bpmn.ModelUtilities.asXml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelector;
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
  void testBpmnFromYaml(BpmnConversionCase testCase) throws Exception {

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
      assertConversionMessagesEqual(asXml(testCase.expectedMessages()), asXml(actualBpmn));
    }
  }

  public static void assertConversionMessagesEqual(String expectedXml, String actualXml) throws Exception {
    List<Node> expected = extractNodes(expectedXml);
    List<Node> actual = extractNodes(actualXml);

    assertEquals(
        expected.size(), 
        actual.size(), 
        () -> {
          StringBuilder sb = new StringBuilder();
          sb.append("Mismatch in number of <conversion:message> elements:\n");
          sb.append("Expected (").append(expected.size()).append("):\n");
          for (Node e : expected) {
            sb.append("- ").append(e.getTextContent().trim()).append("\n");
          }

          sb.append("Actual (").append(actual.size()).append("):\n");
          for (Node a : actual) {
            sb.append("- ").append(a.getTextContent().trim()).append("\n");
          }

          return sb.toString();
        }
    );

    List<Node> unmatched = new ArrayList<>(actual);

    for (final Node expectedNode : expected) {
      boolean matched = unmatched.removeIf(actualNode -> {
        Diff diff = DiffBuilder.compare(Input.fromNode(expectedNode))
            .withTest(Input.fromNode(actualNode))
            .withNodeMatcher(new DefaultNodeMatcher(matchByNameAndSeverityAndTextOnly()))
            .withAttributeFilter(attr -> !"link".equals(attr.getNodeName()))
            .ignoreWhitespace()
            .normalizeWhitespace()
            .checkForSimilar()
            .build();
        return !diff.hasDifferences();
      });

      assertTrue(matched, () -> {
        StringBuilder sb = new StringBuilder();
        sb.append("No matching actual message found for:\n")
          .append(expectedNode.getTextContent().trim())
          .append("\n\nRemaining unmatched actual messages:\n");

        for (Node node : unmatched) {
          sb.append("- ").append(node.getTextContent().trim()).append("\n");
        }

        return sb.toString();
      });
    }
  }

  private static List<Node> extractNodes(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(
        new NamespaceContext() {
          @Override
          public String getNamespaceURI(String prefix) {
            return switch (prefix) {
              case "conversion" -> "http://camunda.org/schema/conversion/1.0";
              default -> XMLConstants.NULL_NS_URI;
            };
          }

          @Override
          public String getPrefix(String namespaceURI) {
            return null;
          }

          @Override
          public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
          }
        });

    NodeList nodes = (NodeList) xpath.evaluate("//conversion:message", doc, XPathConstants.NODESET);

    List<Node> result = new ArrayList<>();
    for (int i = 0; i < nodes.getLength(); i++) result.add(nodes.item(i));
    return result;
  }

  private static ElementSelector matchByNameAndSeverityAndTextOnly() {
    return (control, test) -> {
      if (!control.getNodeName().equals(test.getNodeName())) return false;

      String cSeverity = getAttr(control, "severity");
      String tSeverity = getAttr(test, "severity");

      if (!Objects.equals(cSeverity, tSeverity)) return false;

      // Ignore all other attributes completely
      String cText = control.getTextContent().trim();
      String tText = test.getTextContent().trim();

      return Objects.equals(cText, tText);
    };
  }

  private static String getAttr(Node node, String name) {
    NamedNodeMap attrs = node.getAttributes();
    return attrs != null && attrs.getNamedItem(name) != null
        ? attrs.getNamedItem(name).getNodeValue()
        : null;
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
