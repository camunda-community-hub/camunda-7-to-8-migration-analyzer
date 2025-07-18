package org.camunda.community.migration.converter.bpmn;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;

public class ModelUtilities {

  public static BpmnModelInstance wrapSnippetInProcess(String snippetXml) {
    String fullXml = asC7BpmnXml(snippetXml);

    return Bpmn.readModelFromStream(
        new ByteArrayInputStream(fullXml.getBytes(StandardCharsets.UTF_8)));
  }

  public static String asXml(String snippet) {
    return """
      <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
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

  private static String getSnippetTaskId(String snippetXml) {
    String fullXml = asC7BpmnXml(snippetXml);

    BpmnModelInstance snippetModel =
        Bpmn.readModelFromStream(
            new ByteArrayInputStream(fullXml.getBytes(StandardCharsets.UTF_8)));

    Activity importedActivity =
        snippetModel.getModelElementsByType(Activity.class).stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No activity element found in snippet"));

    return importedActivity.getId();
  }

  public static String extractSnippet(
      BpmnModelInstance modelInstance, String bpmnSnippetXmlForTaskId) {
    String taskId = getSnippetTaskId(bpmnSnippetXmlForTaskId);
    return extractTaskXmlFromBpmn(Bpmn.convertToString(modelInstance), taskId);
  }

  public static String extractTaskXmlFromBpmn(String bpmnXml, String taskId) {
    Pattern pattern =
        Pattern.compile(
            "<(bpmn:[a-zA-Z]+)\\s+[^>]*id=[\"']%s[\"'][^>]*>.*?</\\1>".formatted(taskId),
            Pattern.DOTALL);
    Matcher matcher = pattern.matcher(bpmnXml);
    if (matcher.find()) {
      return matcher.group(0);
    }
    throw new RuntimeException("Task with id '%s' not found in BPMN XML.".formatted(taskId));
  }
}
