package org.camunda.community.migration.converter.visitor.impl.event;

import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.AbstractExecutionListenerConvertible.ZeebeExecutionListener;
import org.camunda.community.migration.converter.convertible.AbstractExecutionListenerConvertible.ZeebeExecutionListener.EventType;
import org.camunda.community.migration.converter.convertible.Convertible;
import org.camunda.community.migration.converter.convertible.StartEventConvertible;
import org.camunda.community.migration.converter.message.MessageFactory;
import org.camunda.community.migration.converter.version.SemanticVersion;
import org.camunda.community.migration.converter.visitor.AbstractEventVisitor;

public class StartEventVisitor extends AbstractEventVisitor {
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
    if (context.getProperties().getAddDataMigrationExecutionListener()
        && isBlankStartEvent(context.getElement())) {
      ZeebeExecutionListener executionListener = new ZeebeExecutionListener();
      executionListener.setEventType(EventType.end);
      executionListener.setListenerType(
          context.getProperties().getDataMigrationExecutionListenerJobType());
      context.addConversion(
          StartEventConvertible.class, s -> s.getZeebeExecutionListeners().add(executionListener));
      context.addMessage(
          MessageFactory.dataMigrationStartListenerAdded(
              executionListener.getListenerType(), context.getElement().getLocalName()));
    }
  }
}
