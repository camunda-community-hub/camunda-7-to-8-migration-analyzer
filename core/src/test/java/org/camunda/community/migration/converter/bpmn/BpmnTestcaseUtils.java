package org.camunda.community.migration.converter.bpmn;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BpmnTestcaseUtils {

  public static BpmnModelInstance wrapSnippetInProcess(String snippetXml) {
    String fullXml = asC7BpmnXml(snippetXml);

    return Bpmn.readModelFromStream(
        new ByteArrayInputStream(fullXml.getBytes(StandardCharsets.UTF_8)));
  }

  public static String asXml(String snippet) {
    return """
      <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                   xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                   xmlns:zeebe="http://camunda.io/schema/zeebe/1.0"
                   xmlns:conversion="http://camunda.org/schema/conversion/1.0">
        <bpmn:process id="process">
          %s
        </bpmn:process>
      </definitions>
      """
        .formatted(snippet);
  }

  private static String asC7BpmnXml(String snippetXml) {
    String fullXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                     xmlns:modeler="http://camunda.org/schema/modeler/1.0"
                     targetNamespace="http://camunda.org/examples"
                     modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.23.0">
          <bpmn:process id="process" isExecutable="true">
            %s
          </bpmn:process>
        </definitions>
        """
            .formatted(snippetXml);
    return fullXml;
  }

  public static String extractSnippet(BpmnModelInstance modelInstance) {
    return extractSnippetFromWrappedBpmn(Bpmn.convertToString(modelInstance));
  }

  public static String extractSnippetFromWrappedBpmn(String fullBpmnXml) {
    Pattern pattern =
        Pattern.compile(
            "<bpmn:process[^>]*>(.*?)</bpmn:process>", Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(fullBpmnXml);

    if (matcher.find()) {
      return matcher.group(1).trim();
    } else {
      throw new IllegalArgumentException(
          "Could not find <bpmn:process>...</bpmn:process> in BPMN XML");
    }
  }

  public static class MessageNode {
    private final String severity;
    private final String text;

    public MessageNode(Node node) {
      this.severity =
          Optional.ofNullable(node.getAttributes().getNamedItem("severity"))
              .map(Node::getTextContent)
              .orElse("");
      this.text = node.getTextContent().trim().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MessageNode that = (MessageNode) o;
      return severity.equals(that.severity) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(severity, text);
    }

    @Override
    public String toString() {
      return severity + ": " + text;
    }

    public String getSeverity() {
      return severity;
    }

    public String getText() {
      return text;
    }
  }

  public static Set<MessageNode> extractMessageNodes(String xml) throws Exception {
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

    Set<MessageNode> result = new HashSet<MessageNode>();
    for (int i = 0; i < nodes.getLength(); i++) {
      result.add(new MessageNode(nodes.item(i)));
    }
    return result;
  }

  public static void applyProperties(Object target, Map<String, String> properties) {
    Class<?> clazz = target.getClass();

    properties.forEach(
        (key, stringValue) -> {
          String methodName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);

          for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
              Class<?> paramType = method.getParameterTypes()[0];

              try {
                Object convertedValue = convert(stringValue, paramType);
                method.invoke(target, convertedValue);
                return; // success
              } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to set property '" + key + "' with value '" + stringValue + "'", e);
              }
            }
          }

          throw new RuntimeException("No matching setter found for property: " + key);
        });
  }

  private static Object convert(String value, Class<?> targetType) {
    if (targetType == String.class) {
      return value;
    } else if (targetType == boolean.class || targetType == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (targetType == int.class || targetType == Integer.class) {
      return Integer.parseInt(value);
    } else if (targetType == long.class || targetType == Long.class) {
      return Long.parseLong(value);
    } else if (targetType == double.class || targetType == Double.class) {
      return Double.parseDouble(value);
    }
    throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
  }
}
