package org.camunda.community.migration.converter.visitor.impl.event;

import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.ZEEBE_NS;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.Convertible;
import org.camunda.community.migration.converter.convertible.StartEventConvertible;
import org.camunda.community.migration.converter.message.MessageFactory;
import org.camunda.community.migration.converter.version.SemanticVersion;
import org.camunda.community.migration.converter.visitor.AbstractEventVisitor;

public class StartEventVisitor extends AbstractEventVisitor {
  @Override
  public String localName() {
    return "startEvent";
  }

  @Override
  protected Convertible createConvertible(DomElementVisitorContext context) {
    return new StartEventConvertible();
  }

  @Override
  protected SemanticVersion availableFrom(DomElementVisitorContext context) {
    return SemanticVersion._8_0;
  }

  @Override
  protected void postCreationVisitor(DomElementVisitorContext context) {
    if (context.getProperties().getAddDataMigrationExecutionListener()) {
      DomElement element = context.getElement();
      DomDocument document = element.getDocument();

      /* ADD TO START EVENT
      <bpmn:extensionElements>
        <zeebe:executionListeners>
          <zeebe:executionListener eventType="end" type="migrator" />
        </zeebe:executionListeners>
      </bpmn:extensionElements>
       */

      if (!isBlankStartEvent(element)) {
        return;
      }

      // Get or create <bpmn:extensionElements>
      DomElement extensionElements = getOrCreate(BPMN20_NS, "extensionElements", element, document);

      // Add <zeebe:executionListeners>
      DomElement executionListeners =
          getOrCreate(ZEEBE_NS, "executionListeners", extensionElements, document);

      // Add <zeebe:executionListener>
      DomElement listenerElement = document.createElement(ZEEBE_NS, "executionListener");
      listenerElement.setAttribute(ZEEBE_NS, "eventType", "end");
      listenerElement.setAttribute(
          ZEEBE_NS, "type", context.getProperties().getDataMigrationExecutionListenerJobType());
      executionListeners.appendChild(listenerElement);

      context.addMessage(MessageFactory.startListenerAdded(element.getLocalName()));
    }
  }

  private DomElement getOrCreate(
      String namespace, String elementName, DomElement element, DomDocument document) {
    List<DomElement> requiredElementList = element.getChildElementsByNameNs(namespace, elementName);

    if (requiredElementList != null && requiredElementList.size() > 0) {
      return requiredElementList.get(0);
    }
    DomElement requiredElement = document.createElement(namespace, elementName);

    // Find last allowed "before" element
    DomElement insertAfter = null;
    for (DomElement child : element.getChildElements()) {
      String localName = child.getLocalName();
      if (BPMN20_NS.equals(child.getNamespaceURI())
          && (localName.equals("property") || localName.equals("documentation"))) {
        insertAfter = child;
      } else {
        break; // Stop at first non-allowed element
      }
    }

    if (insertAfter != null) {
      element.insertChildElementAfter(requiredElement, insertAfter);
    } else {
      // If no "insertAfter" candidate, manually re-insert children after new element
      List<DomElement> existingChildren = new ArrayList<>(element.getChildElements());
      for (DomElement child : existingChildren) {
        element.removeChild(child);
      }
      element.appendChild(requiredElement);
      for (DomElement child : existingChildren) {
        element.appendChild(child);
      }
    }

    return requiredElement;
  }

  public static boolean isBlankStartEvent(DomElement element) {
    if (!"startEvent".equals(element.getLocalName())) {
      return false;
    }

    // Look for any child element ending in EventDefinition (message, timer, signal, etc.)
    for (DomElement child : element.getChildElements()) {
      String localName = child.getLocalName();
      if (localName != null && localName.endsWith("EventDefinition")) {
        return false; // not blank
      }
    }

    return true; // blank: no event definitions
  }
}
