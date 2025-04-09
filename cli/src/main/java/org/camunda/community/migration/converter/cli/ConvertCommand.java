package org.camunda.community.migration.converter.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "camunda-7-to-8-migration-analyzer-cli",
    description = {
      "%nExecute as:",
      "%njava -Dfile.encoding=UTF-8 -jar camunda-7-to-8-migration-analyzer-cli.jar%n"
    },
    mixinStandardHelpOptions = true,
    optionListHeading = "Options:%n",
    parameterListHeading = "Parameter:%n",
    showDefaultValues = true,
    versionProvider = MavenVersionProvider.class,
    subcommands = {ConvertLocalCommand.class, ConvertEngineCommand.class})
public class ConvertCommand {
  public static final Logger LOG_CLI = LoggerFactory.getLogger("cli");

  public static void main(String[] args) {
    int exitCode = new CommandLine(new ConvertCommand()).execute(args);
    System.exit(exitCode);
  }
}
