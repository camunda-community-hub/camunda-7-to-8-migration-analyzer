package org.camunda.community.migration.converter.visitor;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.community.migration.converter.DomElementVisitorContext;
import org.camunda.community.migration.converter.convertible.ServiceTaskConvertible;
import org.camunda.community.migration.converter.message.Message;
import org.camunda.community.migration.converter.message.MessageFactory;

public abstract class AbstractDelegateImplementationVisitor
    extends AbstractSupportedAttributeVisitor {
  public static final Pattern DELEGATE_NAME_EXTRACT = Pattern.compile("[#$]\\{(.*)}");
  private static final Set<String> IGNORE =
      Stream.of("taskListener", "executionListener", "errorEventDefinition")
          .collect(Collectors.toSet());

  @Override
  protected Message visitSupportedAttribute(DomElementVisitorContext context, String attribute) {
    if (context.getProperties().getKeepJobTypeBlank()) {
      return MessageFactory.delegateImplementationNoDefaultJobType(attributeLocalName(), attribute);
    } else {
      if (context.getProperties().getAlwaysUseDefaultJobType()) {
        context.addConversion(
            ServiceTaskConvertible.class,
            serviceTaskConversion ->
                serviceTaskConversion.addZeebeTaskHeader(attributeLocalName(), attribute));
        context.addConversion(
            ServiceTaskConvertible.class,
            serviceTaskConversion ->
                serviceTaskConversion
                    .getZeebeTaskDefinition()
                    .setType(context.getProperties().getDefaultJobType()));
        return MessageFactory.delegateImplementation(
            attributeLocalName(),
            context.getElement().getLocalName(),
            attribute,
            context.getProperties().getDefaultJobType());
      } else {  // use delegate expression itself to form job type        
        if (attribute == null || attribute.trim().isEmpty()) {
           MessageFactory.delegateExpressionAsJobTypeNull(attribute);
        }

        // Check for valid simple expression
        // #{sampleBean}  --> simpleBean
        // #{sampleBean.property} --> sampleBeanProperty
        Matcher matcher = SIMPLE_EXPRESSION_PATTERN.matcher(attribute);
        if (matcher.matches()) {
            String bean = matcher.group(1);
            String property = matcher.group(2);
            if (property != null) {
              bean = bean + capitalize(property);
            }
            final String jobType = bean;
            context.addConversion(
                ServiceTaskConvertible.class,
                serviceTaskConversion ->
                    serviceTaskConversion.getZeebeTaskDefinition().setType(jobType));
            return MessageFactory.delegateExpressionAsJobType(jobType, attribute);
        }

        // If it's an expression, but doesn't match the simple pattern --> treat as method 
        // use default job type and add header
        if (EXPRESSION_WRAPPER_PATTERN.matcher(attribute).matches()) {
          final String jobType = context.getProperties().getDefaultJobType();
          context.addConversion(
              ServiceTaskConvertible.class,
              serviceTaskConversion ->
                  serviceTaskConversion.addZeebeTaskHeader(attributeLocalName(), attribute));
          context.addConversion(
              ServiceTaskConvertible.class,
              serviceTaskConversion ->
                  serviceTaskConversion.getZeebeTaskDefinition().setType(jobType));

          return
              MessageFactory.expressionMethodAsJobType(
                jobType,                
                attribute);
        }

        // FQN --> extract class name and decapitalize
        // com.foo.MyDelegate --> myDelegate
        if (attribute.contains(".")) {
            int lastDot = attribute.lastIndexOf('.');
            final String jobType = decapitalize(attribute.substring(lastDot + 1));
            
            context.addConversion(
                ServiceTaskConvertible.class,
                serviceTaskConversion ->
                    serviceTaskConversion.getZeebeTaskDefinition().setType(jobType));
            return MessageFactory.delegateExpressionAsJobType(jobType, attribute);            
        }        
                
        // Fallback - just use what we have
        final String jobType = attribute;
        context.addConversion(
            ServiceTaskConvertible.class,
            serviceTaskConversion ->
                serviceTaskConversion.getZeebeTaskDefinition().setType(jobType));
        return MessageFactory.delegateExpressionAsJobType(jobType, attribute);
      }
    }
  }
//
//  private String extractJobType(String attribute) {
//    Matcher matcher = DELEGATE_NAME_EXTRACT.matcher(attribute);
//
//    if (matcher.matches()) {
//        // Expression detected (e.g. #{bean.foo})
//        return matcher.group(1);
//    } else if (attribute.contains(".")) {
//        // Likely a fully-qualified class name
//        int lastDot = attribute.lastIndexOf('.');
//        // return classname but with lower case first character
//        return decapitalize(attribute.substring(lastDot + 1));
//    } else {
//        return null;
//    }
//  }  
  
  private static final Pattern SIMPLE_EXPRESSION_PATTERN =
      Pattern.compile("[#$]\\{([a-zA-Z_][a-zA-Z0-9_]*)(?:\\.([a-zA-Z_][a-zA-Z0-9_]*))?}");

  private static final Pattern EXPRESSION_WRAPPER_PATTERN =
      Pattern.compile("[#$]\\{(.*)}");

  public String extractJobType(DomElementVisitorContext context, final String attribute, String defaultJobType) {


      // No expression, no dot --> return raw
      return attribute;
  }

  
  private String decapitalize(String name) {
    if (name == null || name.isEmpty()) return name;
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }
  
  private String capitalize(String name) {
      if (name == null || name.isEmpty()) return name;
      return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }


  @Override
  protected boolean canVisit(DomElementVisitorContext context) {
    return super.canVisit(context) && !IGNORE.contains(context.getElement().getLocalName());
  }
}
