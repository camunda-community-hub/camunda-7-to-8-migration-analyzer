# Migration Analyzer

The Migration Analyzer:

- Analyzes Camunda 7 models (BPMN & DMN) to identify tasks required for migration to Camunda 8.
- Converts those models.

The conversion process can be extended to accommodate special requirements.

## Table of Contents

- [Installation](#installation)
  - [SaaS](#saas)
  - [Local Java Installation](#local-java-installation)
  - [Docker](#docker)
  - [CLI](#cli)
  - [Building from Source](#building-from-source) or [Embedding into your own Java Application](#embedding-into-your-own-java-applications)
  - [License](#license)
- [How to Use](https://docs.camunda.io/docs/guides/migrating-from-camunda-7/migration-tooling/#migration-analyzer) (Refers to the [Camunda Migration Guide](https://docs.camunda.io/docs/guides/migrating-from-camunda-7/))
- [How to Extend](#how-to-extend-diagram-conversion)

## Installation

You can use the Migration Analyzer in the following ways:

- **Web Interface**: A locally installed web-based wizard for the Migration Analyzer, implemented as a Java Spring application. This can be installed
  - locally as Java jar,
  - using Docker, or
  - consumed as SaaS from our free hosted version.
- **CLI**: A Command-Line Interface for the Migration Analyzer, implemented as a Java application.

### SaaS

A free hosted version of the Migration Analyzer is available at [https://diagram-converter.consulting-sandbox.camunda.cloud/](https://diagram-converter.consulting-sandbox.camunda.cloud/). 

Note that your models are **not** stored on this platform, and given there is https on transit, your models are safe. However, we don't give any gurantees on this free SaaS version.

### Local Java Installation

You can also install the web application locally.

**Requirements**:
- Java 17 or higher

**Steps**:

1. Download the latest Migration Analyzer web application: [camunda-7-to-8-migration-analyzer-webapp.jar](https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer/releases/latest/download/camunda-7-to-8-migration-analyzer-webapp.jar).
2. Run the application. Navigate to the directory where the `.jar` file was downloaded and execute the following command:

   ```shell
   java -jar camunda-7-to-8-migration-analyzer-webapp.jar
   ```

3. Access the web application at [http://localhost:8080/](http://localhost:8080/).

To run the application on a different port, use the following command:

```shell
java -Dserver.port=8090 -jar camunda-7-to-8-migration-analyzer-webapp.jar
```

### Docker

You can also run the Migration Analyzer using Docker.

1. Pull the latest version of the Docker image:

   ```shell
   docker pull ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
   ```

2. Run the Docker container and expose port 8080:

   ```shell
   docker run -p 8080:8080 ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
   ```

3. Access the web application at [http://localhost:8080/](http://localhost:8080/).

### CLI (Command Line Interface)

The CLI is a standalone Java application.

**Steps**:

1. Download the latest Migration Analyzer CLI application: [camunda-7-to-8-migration-analyzer-cli.jar](https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer/releases/latest/download/camunda-7-to-8-migration-analyzer-cli.jar).
2. Run the CLI with the following command:

   ```shell
   java -jar camunda-7-to-8-migration-analyzer-cli.jar --help
   ```
  The typical way is to run it in `local` mode and reference your BPMN model file: 
   ```shell
   java -jar camunda-7-to-8-migration-analyzer-cli.jar local myBpmnModel.bpmn
   ```

### Building from Source

You can build this project from source using Maven.

**Steps**:

1. Clone the repository.
2. Run the following command to build the project:

   ```shell
   mvn clean package
   ```

### Embedding into your own Java Applications

You can also embed the core diagram conversion as library into your own Java application. This is [explained here](core/README.md).

## How to Extend Diagram Conversion?

You can extend diagram conversion by leveraging the SPI. You can find an example in the [extension example project](extension-example/).

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).