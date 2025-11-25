package org.camunda.community.migration.converter.cli;

import static org.camunda.community.migration.converter.cli.ConvertCommand.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.community.migration.converter.ConverterPropertiesFactory;
import org.camunda.community.migration.converter.DefaultConverterProperties;
import org.camunda.community.migration.converter.DiagramCheckResult;
import org.camunda.community.migration.converter.DiagramConverter;
import org.camunda.community.migration.converter.DiagramConverterFactory;
import org.camunda.community.migration.converter.excel.ExcelWriter;
import picocli.CommandLine.Option;

public abstract class AbstractConvertCommand implements Callable<Integer> {
  private static final String DEFAULT_PREFIX = "converted-c8-";

  protected final DiagramConverter converter;
  protected int returnCode = 0;

  @Option(
      names = {"-d", "--documentation"},
      description = "If enabled, messages are also appended to documentation")
  boolean documentation;

  @Option(
      names = {"--default-job-type"},
      description =
          "Job type used when adjusting delegates. If set, the default value from the 'converter-properties.properties' is overridden")
  String defaultJobType;

  @Option(
      names = {"--prefix"},
      description = "Prefix for the name of the generated file",
      defaultValue = DEFAULT_PREFIX)
  String prefix = DEFAULT_PREFIX;

  @Option(
      names = {"-o", "--override"},
      description = "If enabled, existing files are overridden")
  boolean override;

  @Option(
      names = {"--platform-version"},
      description = "Semantic version of the target platform, defaults to latest version")
  String platformVersion;

  @Option(
      names = {"--csv"},
      description =
          "If enabled, a CSV file will be created containing the results for the analysis")
  boolean csv;

  @Option(
      names = {"--xlsx"},
      description =
          "If enabled, a XLSX file will be created containing the results for the analysis")
  boolean xlsx;

  @Option(
      names = {"--md", "--markdown"},
      description =
          "If enabled, a markdown file will be created containing the results for all conversions")
  boolean markdown;

  @Option(names = "--check", description = "If enabled, no converted diagrams are exported")
  boolean check;

  @Option(
      names = "--disable-append-elements",
      description = "Disables adding conversion messages to the bpmn xml")
  boolean disableAppendElements;

  @Option(
      names = "--keep-job-type-blank",
      description =
          "Sets all job types to blank so that you need to edit those after conversion yourself")
  boolean keepJobTypeBlank;

  @Option(
      names = "--always-use-default-job-type",
      description =
          "Always fill in the configured default job type, interesting if you want to use one delegation job worker (like the Camunda 7 Adapter).")
  boolean alwaysUseDefaultJobType;

  @Option(
      names = "--add-data-migration-execution-listener",
      description =
          "Add an execution listener on blank start events that can be used for the Camunda 7 Data Migrator")
  boolean addDataMigrationExecutionListener;

  @Option(
      names = "--data-migration-execution-listener-job-type",
      description =
          "Name of the job type of the listener. If set, the default value from the 'converter-properties.properties' is overridden")
  String dataMigrationExecutionListenerJobType;

  public AbstractConvertCommand() {
    DiagramConverterFactory factory = DiagramConverterFactory.getInstance();
    factory.getNotificationServiceFactory().setInstance(new PrintNotificationServiceImpl());
    converter = factory.get();
  }

  @Override
  public final Integer call() {
    returnCode = 0;
    Map<File, ModelInstance> modelInstances = modelInstances();
    List<DiagramCheckResult> results = checkModels(modelInstances);
    writeResults(modelInstances, results);
    return returnCode;
  }

  private void writeResults(
      Map<File, ModelInstance> modelInstances, List<DiagramCheckResult> results) {
    if (!check) {
      for (Entry<File, ModelInstance> modelInstance : modelInstances.entrySet()) {
        File file = determineFileName(prefixFileName(modelInstance.getKey()));
        if (!override && file.exists()) {
          LOG_CLI.error("File does already exist: {}", file);
          returnCode = 1;
        }
        LOG_CLI.info("Created {}", file);
        try (FileWriter fw = new FileWriter(file)) {
          converter.printXml(modelInstance.getValue().getDocument(), true, fw);
          fw.flush();
        } catch (IOException e) {
          LOG_CLI.error("Error while creating BPMN file: {}", createMessage(e));
          returnCode = 1;
        }
      }
    }
    if (csv) {
      File csvFile = determineFileName(new File(targetDirectory(), "analysis-results.csv"));
      try (FileWriter fw = new FileWriter(csvFile)) {
        converter.writeCsvFile(results, fw);
        LOG_CLI.info("Created {}", csvFile);
      } catch (IOException e) {
        LOG_CLI.error("Error while creating csv results: {}", createMessage(e));
        returnCode = 1;
      }
    }
    if (xlsx) {
      File xlsxFile = determineFileName(new File(targetDirectory(), "analysis-results.xlsx"));
      try (FileOutputStream fos = new FileOutputStream(xlsxFile)) {
        new ExcelWriter().writeResultsToExcel(converter.createLineItemDTOList(results), fos);
        LOG_CLI.info("Created {}", xlsxFile);
      } catch (IOException e) {
        LOG_CLI.error("Error while creating xlsx results: {}", createMessage(e));
        returnCode = 1;
      }
    }
    if (markdown) {
      File markdownFile = determineFileName(new File(targetDirectory(), "analysis-results.md"));
      try (FileWriter fw = new FileWriter(markdownFile)) {
        converter.writeMarkdownFile(results, fw);
        LOG_CLI.info("Created {}", markdownFile);
      } catch (IOException e) {
        LOG_CLI.error("Error while creating markdown results: {}", createMessage(e));
        returnCode = 1;
      }
    }
  }

  protected abstract File targetDirectory();

  private List<DiagramCheckResult> checkModels(Map<File, ModelInstance> modelInstances) {
    return modelInstances.entrySet().stream()
        .map(this::checkModel)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private DiagramCheckResult checkModel(Entry<File, ModelInstance> modelInstance) {
    try {
      return converter.check(
          modelInstance.getKey().getPath(),
          modelInstance.getValue(),
          ConverterPropertiesFactory.getInstance().merge(converterProperties()));
    } catch (Exception e) {
      LOG_CLI.error("Problem while converting: {}", createMessage(e));
      returnCode = 1;
      return null;
    }
  }

  protected abstract Map<File, ModelInstance> modelInstances();

  protected DefaultConverterProperties converterProperties() {
    DefaultConverterProperties properties = new DefaultConverterProperties();
    properties.setDefaultJobType(defaultJobType);
    properties.setPlatformVersion(platformVersion);
    properties.setAppendDocumentation(documentation);
    properties.setAppendElements(!disableAppendElements);
    properties.setKeepJobTypeBlank(keepJobTypeBlank);
    properties.setAlwaysUseDefaultJobType(alwaysUseDefaultJobType);
    properties.setAddDataMigrationExecutionListener(addDataMigrationExecutionListener);
    properties.setDataMigrationExecutionListenerJobType(dataMigrationExecutionListenerJobType);

    return properties;
  }

  private File prefixFileName(File file) {
    return new File(file.getParentFile(), prefix + file.getName());
  }

  private File determineFileName(File file) {
    File newFile = file;
    int counter = 0;
    while (!override && newFile.exists()) {
      counter++;
      newFile =
          new File(
              file.getParentFile(),
              FilenameUtils.getBaseName(file.getName())
                  + " ("
                  + counter
                  + ")."
                  + FilenameUtils.getExtension(file.getName()));
    }
    return newFile;
  }

  protected String createMessage(Exception e) {
    StringBuilder message = new StringBuilder(e.getMessage());
    Throwable ex = e.getCause();
    while (ex != null) {
      message.append(",").append("\n").append("caused by: ").append(ex.getMessage());
      ex = ex.getCause();
    }
    return message.toString();
  }
}
