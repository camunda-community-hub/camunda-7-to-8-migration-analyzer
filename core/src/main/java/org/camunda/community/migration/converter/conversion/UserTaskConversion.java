package org.camunda.community.migration.converter.conversion;

import static org.camunda.community.migration.converter.BpmnElementFactory.getExtensionElements;
import static org.camunda.community.migration.converter.NamespaceUri.ZEEBE;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.convertible.UserTaskConvertible;
import org.camunda.community.migration.converter.convertible.UserTaskConvertible.ZeebeTaskListener;

public class UserTaskConversion extends AbstractTypedConversion<UserTaskConvertible> {
  @Override
  protected void convertTyped(DomElement element, UserTaskConvertible convertible) {
    DomElement extensionElements = getExtensionElements(element);
    if (convertible.isZeebeUserTask()) {
      extensionElements.appendChild(createZeebeUserTask(element.getDocument()));
    }

    if (canAddAssignmentDefinition(convertible)) {
      extensionElements.appendChild(createAssignmentDefinition(element.getDocument(), convertible));
    }
    if (canAddTaskSchedule(convertible)) {
      extensionElements.appendChild(createTaskSchedule(element.getDocument(), convertible));
    }
    if (hasTaskListeners(convertible)) {
      DomElement listenersRoot = createTaskListeners(extensionElements);
      for (ZeebeTaskListener listener : convertible.getZeebeTaskListeners()) {
        createTaskListener(listenersRoot, listener);
      }
    }
  }

  private boolean hasTaskListeners(UserTaskConvertible convertible) {
    return convertible.getZeebeTaskListeners() != null
        && !convertible.getZeebeTaskListeners().isEmpty();
  }

  private DomElement createZeebeUserTask(DomDocument document) {
    return document.createElement(ZEEBE, "userTask");
  }

  private DomElement createTaskSchedule(DomDocument document, UserTaskConvertible convertible) {
    DomElement taskSchedule = document.createElement(ZEEBE, "taskSchedule");
    taskSchedule.setAttribute("dueDate", convertible.getZeebeTaskSchedule().getDueDate());
    taskSchedule.setAttribute("followUpDate", convertible.getZeebeTaskSchedule().getFollowUpDate());
    return taskSchedule;
  }

  private boolean canAddTaskSchedule(UserTaskConvertible convertible) {
    return convertible.getZeebeTaskSchedule().getDueDate() != null
        || convertible.getZeebeTaskSchedule().getFollowUpDate() != null;
  }

  private DomElement createAssignmentDefinition(
      DomDocument document, UserTaskConvertible convertible) {
    DomElement assignmentDefinition = document.createElement(ZEEBE, "assignmentDefinition");
    assignmentDefinition.setAttribute(
        "assignee", convertible.getZeebeAssignmentDefinition().getAssignee());
    assignmentDefinition.setAttribute(
        "candidateGroups", convertible.getZeebeAssignmentDefinition().getCandidateGroups());
    assignmentDefinition.setAttribute(
        "candidateUsers", convertible.getZeebeAssignmentDefinition().getCandidateUsers());
    return assignmentDefinition;
  }

  private boolean canAddAssignmentDefinition(UserTaskConvertible convertible) {
    return convertible.getZeebeAssignmentDefinition().getAssignee() != null
        || convertible.getZeebeAssignmentDefinition().getCandidateGroups() != null
        || convertible.getZeebeAssignmentDefinition().getCandidateUsers() != null;
  }

  private void createTaskListener(DomElement listenerRoot, ZeebeTaskListener listener) {
    DomElement executionListenerDom =
        listenerRoot.getDocument().createElement(ZEEBE, "taskListener");
    executionListenerDom.setAttribute(ZEEBE, "eventType", listener.getEventType().c8name());
    executionListenerDom.setAttribute(ZEEBE, "type", listener.getListenerType());
    if (StringUtils.isNotBlank(listener.getRetries())) {
      executionListenerDom.setAttribute(ZEEBE, "retries", listener.getRetries());
    }
    listenerRoot.appendChild(executionListenerDom);
  }

  private DomElement createTaskListeners(DomElement extensionElements) {
    DomElement listeners = extensionElements.getDocument().createElement(ZEEBE, "taskListeners");
    extensionElements.appendChild(listeners);
    return listeners;
  }

  @Override
  protected Class<UserTaskConvertible> type() {
    return UserTaskConvertible.class;
  }
}
