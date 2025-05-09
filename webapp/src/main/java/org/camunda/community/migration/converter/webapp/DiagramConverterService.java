package org.camunda.community.migration.converter.webapp;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.community.migration.converter.ConverterPropertiesFactory;
import org.camunda.community.migration.converter.DefaultConverterProperties;
import org.camunda.community.migration.converter.DiagramCheckResult;
import org.camunda.community.migration.converter.DiagramConverter;
import org.camunda.community.migration.converter.DiagramConverterResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiagramConverterService {
  private final DiagramConverter diagramConverter;

  @Autowired
  public DiagramConverterService(DiagramConverter diagramConverter) {
    this.diagramConverter = diagramConverter;
  }

  public void convert(
      ModelInstance modelInstance,
      boolean appendDocumentation,
      String defaultJobType,
      String platformVersion,
      Boolean defaultJobTypeEnabled) {
    DefaultConverterProperties adaptedProperties = new DefaultConverterProperties();
    adaptedProperties.setDefaultJobType(defaultJobType);
    adaptedProperties.setPlatformVersion(platformVersion);
    adaptedProperties.setDefaultJobTypeEnabled(defaultJobTypeEnabled);
    adaptedProperties.setAppendDocumentation(appendDocumentation);
    diagramConverter.convert(
        modelInstance, ConverterPropertiesFactory.getInstance().merge(adaptedProperties));
  }

  public DiagramCheckResult check(
      String filename,
      ModelInstance modelInstance,
      String defaultJobType,
      String platformVersion,
      Boolean defaultJobTypeEnabled) {
    DefaultConverterProperties adaptedProperties = new DefaultConverterProperties();
    adaptedProperties.setDefaultJobType(defaultJobType);
    adaptedProperties.setPlatformVersion(platformVersion);
    adaptedProperties.setDefaultJobTypeEnabled(defaultJobTypeEnabled);
    return diagramConverter.check(
        filename, modelInstance, ConverterPropertiesFactory.getInstance().merge(adaptedProperties));
  }

  public String printXml(DomDocument document, boolean prettyPrint) {
    try (StringWriter sw = new StringWriter()) {
      diagramConverter.printXml(document, prettyPrint, sw);
      return sw.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeCsvFile(List<DiagramCheckResult> results, Writer writer) {
    diagramConverter.writeCsvFile(results, writer);
  }

  public List<DiagramConverterResultDTO> createLineItemDTOList(List<DiagramCheckResult> results) {
    return diagramConverter.createLineItemDTOList(results);
  }
}
