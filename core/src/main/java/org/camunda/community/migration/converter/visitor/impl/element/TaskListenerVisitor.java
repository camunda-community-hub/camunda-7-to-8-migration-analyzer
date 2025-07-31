package org.camunda.community.migration.converter.visitor.impl.element;

import static org.camunda.community.migration.converter.visitor.AbstractDelegateImplementationVisitor.DELEGATE_NAME_EXTRACT;

import java.util.Optional;
import java.util.regex.Matcher;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.UserTaskConvertible;
import org.camunda.community.migration.converter.convertible.UserTaskConvertible.ZeebeTaskListener;
import org.camunda.community.migration.converter.convertible.UserTaskConvertible.ZeebeTaskListener.EventType;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;
import org.camunda.community.migration.converter.version.SemanticVersion;
import org.camunda.community.migration.converter.visitor.AbstractListenerVisitor;
import org.camunda.community.migration.converter.visitor.AbstractListenerVisitor.ListenerImplementation.DelegateExpressionImplementation;

public class TaskListenerVisitor extends AbstractListenerVisitor {
  @Override
  public String localName() {
    return "taskListener";
  }

  @Override
  protected Message visitListener(
      DomElementVisitorContext context, String event, ListenerImplementation implementation) {
    if (isTaskListenerSupported(context)) {
      ZeebeTaskListener taskListener = new ZeebeTaskListener();
      taskListener.setEventType(EventType.fromName(event).get());

      if (implementation instanceof DelegateExpressionImplementation) {
        Matcher matcher = DELEGATE_NAME_EXTRACT.matcher(implementation.implementation());
        String delegateName = matcher.find() ? matcher.group(1) : implementation.implementation();
        taskListener.setListenerType(delegateName);
      } else {
        taskListener.setListenerType(implementation.implementation());
      }
      context.addConversion(UserTaskConvertible.class, c -> c.addZeebeTaskListener(taskListener));
      return MessageFactory.taskListenerSupported(event, implementation.implementation());
    }
    return MessageFactory.taskListenerNotSupported(
        event, ListenerImplementation.type(implementation), implementation.implementation());
  }

  private boolean isTaskListenerSupported(DomElementVisitorContext context) {
    Optional<EventType> eventType = EventType.fromName(findEventName(context));
    SemanticVersion semanticVersion =
        SemanticVersion.parse(context.getProperties().getPlatformVersion());
    return (semanticVersion.ordinal() >= SemanticVersion._8_8.ordinal())
        && eventType.isPresent()
        && eventType.get().isMapped();
  }

  @Override
  public boolean canBeTransformed(DomElementVisitorContext context) {
    return isTaskListenerSupported(context);
  }
}
