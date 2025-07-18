package org.camunda.community.migration.converter.bpmn;

import java.io.InputStream;
import java.util.List;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class BpmnConversionCaseLoader {

  /**
   * Load from a structure like: categories: - category: scriptTask cases: - name: Convert inline
   * Groovy script givenBpmn: | <bpmn:scriptTask id="script1" ... /> expectedBpmn: |
   * <bpmn:serviceTask id="script1" ... /> expectedMessages: | <conversion:message
   * severity="REVIEW">Something</conversion:message>
   */
  public static List<BpmnConversionCase> loadFromYaml(InputStream inputStream) {
    LoaderOptions options = new LoaderOptions();
    Constructor constructor = new Constructor(BpmnConversionCaseListWrapper.class, options);

    TypeDescription wrapperDesc = new TypeDescription(BpmnConversionCaseListWrapper.class);
    wrapperDesc.addPropertyParameters("categories", Category.class);
    constructor.addTypeDescription(wrapperDesc);

    Yaml yaml = new Yaml(constructor);
    BpmnConversionCaseListWrapper wrapper = yaml.load(inputStream);
    return wrapper.categories.stream().flatMap(category -> category.cases.stream()).toList();
  }

  public static class Category {
    public String category;
    public List<BpmnConversionCase> cases;

    public Category() {}
  }

  public static class BpmnConversionCase {
    public String name;
    public String description;
    public String givenBpmn;
    public String expectedBpmn;
    public String expectedMessages;

    public String name() {
      return name;
    }

    public String description() {
      return description;
    }

    public String givenBpmn() {
      return givenBpmn;
    }

    public String expectedBpmn() {
      return expectedBpmn;
    }

    public String expectedMessages() {
      return expectedMessages;
    }

    @Override
    public String toString() {
      return "BpmnConversionCase [" + name + "]";
    }

    public String fileName() {
      if (name == null) return "unnamed.bpmn";
      String sanitized =
          name.trim()
              .toLowerCase()
              .replaceAll(
                  "[^a-z0-9-_]", "_") // replace anything not alphanumeric, dash or underscore
              .replaceAll("_+", "_") // collapse multiple underscores
              .replaceAll("^_+|_+$", ""); // trim leading/trailing underscores
      return sanitized + ".bpmn";
    }
  }

  public static class BpmnConversionCaseListWrapper {
    public List<Category> categories;

    public BpmnConversionCaseListWrapper() {}
  }
}
