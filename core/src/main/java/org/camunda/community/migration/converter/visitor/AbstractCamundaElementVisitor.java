package org.camunda.community.migration.converter.visitor;

import java.util.List;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.NamespaceUri;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.version.SemanticVersion;

public abstract class AbstractCamundaElementVisitor extends AbstractElementVisitor {
  @Override
  protected List<String> namespaceUri() {
    return List.of(NamespaceUri.CAMUNDA);
  }

  @Override
  protected final void visitElement(DomElementVisitorContext context) {
    Message message = visitCamundaElement(context);
    context.addMessage(message);
  }

  protected boolean isOnBpmnElement(DomElementVisitorContext context, String bpmnElementLocalName) {
    DomElement element = context.getElement();
    while (!element.getLocalName().equals(bpmnElementLocalName)) {
      element = element.getParentElement();
      if (element == null) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected SemanticVersion availableFrom(DomElementVisitorContext context) {
    return SemanticVersion._8_0;
  }

  protected abstract Message visitCamundaElement(DomElementVisitorContext context);

  public abstract boolean canBeTransformed(DomElementVisitorContext context);
}
