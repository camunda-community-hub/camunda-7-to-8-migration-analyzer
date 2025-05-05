package org.camunda.community.migration.converter.conversion;

import static org.camunda.community.migration.converter.BpmnElementFactory.*;
import static org.camunda.community.migration.converter.NamespaceUri.*;
import static org.camunda.community.migration.converter.NamespaceUri.ZEEBE;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.convertible.FormDefinitionConvertible;

public class FormDefinitionConversion extends AbstractTypedConversion<FormDefinitionConvertible> {

  @Override
  protected Class<FormDefinitionConvertible> type() {
    return FormDefinitionConvertible.class;
  }

  @Override
  protected void convertTyped(DomElement element, FormDefinitionConvertible convertible) {
    if (canAddFormDefinition(convertible)) {
      DomElement extensionElements = getExtensionElements(element);
      extensionElements.appendChild(createFormDefinition(element.getDocument(), convertible));
    }
  }

  private boolean canAddFormDefinition(FormDefinitionConvertible convertible) {
    return convertible.getZeebeFormDefinition().getFormKey() != null
        || convertible.getZeebeFormDefinition().getFormId() != null;
  }

  private DomElement createFormDefinition(
      DomDocument document, FormDefinitionConvertible convertible) {
    DomElement formDefinition = document.createElement(ZEEBE, "formDefinition");
    if (convertible.getZeebeFormDefinition().getFormKey() != null) {
      if (convertible.isZeebeUserTask()) {
        formDefinition.setAttribute(
            ZEEBE, "externalReference", convertible.getZeebeFormDefinition().getFormKey());
      } else {
        formDefinition.setAttribute(
            ZEEBE, "formKey", convertible.getZeebeFormDefinition().getFormKey());
      }
    }
    if (convertible.getZeebeFormDefinition().getFormId() != null) {
      formDefinition.setAttribute(
          ZEEBE, "formId", convertible.getZeebeFormDefinition().getFormId());
    }
    if (convertible.getZeebeFormDefinition().getBindingType() != null) {
      formDefinition.setAttribute(
          ZEEBE, "bindingType", convertible.getZeebeFormDefinition().getBindingType().name());
    }
    if (StringUtils.isNotBlank(convertible.getZeebeFormDefinition().getVersionTag())) {
      formDefinition.setAttribute(
          ZEEBE, "versionTag", convertible.getZeebeFormDefinition().getVersionTag());
    }
    return formDefinition;
  }
}
