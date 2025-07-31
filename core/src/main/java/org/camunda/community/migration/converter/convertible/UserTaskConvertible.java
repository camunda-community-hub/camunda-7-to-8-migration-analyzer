package org.camunda.community.migration.converter.convertible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UserTaskConvertible extends AbstractActivityConvertible
    implements FormDefinitionConvertible {
  private final ZeebeFormDefinition zeebeFormDefinition = new ZeebeFormDefinition();
  private final ZeebeAssignmentDefinition zeebeAssignmentDefinition =
      new ZeebeAssignmentDefinition();
  private final ZeebeTaskSchedule zeebeTaskSchedule = new ZeebeTaskSchedule();
  private boolean zeebeUserTask;

  private List<ZeebeTaskListener> zeebeTaskListeners = new ArrayList<>();

  public List<ZeebeTaskListener> getZeebeTaskListeners() {
    return zeebeTaskListeners;
  }

  public void setZeebeTaskListeners(List<ZeebeTaskListener> zeebeTaskListeners) {
    this.zeebeTaskListeners = zeebeTaskListeners;
  }

  public void addZeebeTaskListener(ZeebeTaskListener zeebeTaskListener) {
    zeebeTaskListeners.add(zeebeTaskListener);
  }

  public ZeebeTaskSchedule getZeebeTaskSchedule() {
    return zeebeTaskSchedule;
  }

  public ZeebeFormDefinition getZeebeFormDefinition() {
    return zeebeFormDefinition;
  }

  public ZeebeAssignmentDefinition getZeebeAssignmentDefinition() {
    return zeebeAssignmentDefinition;
  }

  public boolean isZeebeUserTask() {
    return zeebeUserTask;
  }

  public void setZeebeUserTask(boolean zeebeUserTask) {
    this.zeebeUserTask = zeebeUserTask;
  }

  public static class ZeebeFormDefinition {
    private String formId;
    private String formKey;
    private ZeebeFormDefinitionBindingType bindingType;
    private String versionTag;

    public String getFormId() {
      return formId;
    }

    public void setFormId(String formId) {
      this.formId = formId;
    }

    public String getVersionTag() {
      return versionTag;
    }

    public void setVersionTag(String versionTag) {
      this.versionTag = versionTag;
    }

    public ZeebeFormDefinitionBindingType getBindingType() {
      return bindingType;
    }

    public void setBindingType(ZeebeFormDefinitionBindingType bindingType) {
      this.bindingType = bindingType;
    }

    public String getFormKey() {
      return formKey;
    }

    public void setFormKey(String formKey) {
      this.formKey = formKey;
    }

    public enum ZeebeFormDefinitionBindingType {
      deployment,
      versionTag
    }
  }

  public static class ZeebeAssignmentDefinition {
    private String assignee;
    private String candidateGroups;
    private String candidateUsers;

    public String getCandidateUsers() {
      return candidateUsers;
    }

    public void setCandidateUsers(String candidateUsers) {
      this.candidateUsers = candidateUsers;
    }

    public String getAssignee() {
      return assignee;
    }

    public void setAssignee(String assignee) {
      this.assignee = assignee;
    }

    public String getCandidateGroups() {
      return candidateGroups;
    }

    public void setCandidateGroups(String candidateGroup) {
      this.candidateGroups = candidateGroup;
    }
  }

  public static class ZeebeTaskSchedule {
    private String dueDate;
    private String followUpDate;

    public String getDueDate() {
      return dueDate;
    }

    public void setDueDate(String dueDate) {
      this.dueDate = dueDate;
    }

    public String getFollowUpDate() {
      return followUpDate;
    }

    public void setFollowUpDate(String followUpDate) {
      this.followUpDate = followUpDate;
    }
  }

  public static class ZeebeTaskListener {
    private String listenerType;
    private String retries;
    private EventType eventType;

    public String getListenerType() {
      return listenerType;
    }

    public void setListenerType(String listenerType) {
      this.listenerType = listenerType;
    }

    public String getRetries() {
      return retries;
    }

    public void setRetries(String retries) {
      this.retries = retries;
    }

    public EventType getEventType() {
      return eventType;
    }

    public void setEventType(EventType eventType) {
      this.eventType = eventType;
    }

    public enum EventType {
      CREATE("creating"),
      ASSIGNMENT("assigning"),
      UPDATE("updating"),
      COMPLETE("completing"),
      DELETE("canceling"),
      TIMEOUT(null); // not mapped: Camunda 8 does not support a corresponding timeout event type.

      private final String camunda8Name;

      EventType(String camunda8Name) {
        this.camunda8Name = camunda8Name;
      }

      public String c8name() {
        return camunda8Name;
      }

      public static Optional<EventType> fromName(String name) {
        return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name)).findFirst();
      }

      public boolean isMapped() {
        return (camunda8Name != null);
      }
    }
  }
}
