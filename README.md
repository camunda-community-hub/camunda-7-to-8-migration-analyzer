# Migration Analyzer

The migration analyzer can do tasks for you:

* Analyze Camunda 7 models (BPMN & DMN) to find tasks you would need to do to migrate them to Camunda 8
* Convert those models

The actual convertion can be extended if you have special needs.

## Installation

You can use the migration analyzer in different ways:

- Web application: Local installation of a web-based wizard to use the migration analyzer. Technically a Java Spring application.
- CLI: A Command-Line-Interface for the migration analyzer. Technically a Java Spring application.
- SaaS: Access the migration analyzer in a free hosted version: https://diagram-converter.consulting-sandbox.camunda.cloud/. We don't store any of your data there!

### Web application

#### Local installation

Requirements:
- Java >= 17

1. Download the latest migration analyzer as ZIP archive: TODO
2. Unzip TODO
3. Run TODO
4. Access the web application: http://localhost:8080/

#### Docker

You can also run the migration analyzer via Docker.

1. Pull the latest version:

```shell
docker pull ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
```

2. Run the image and expose the port 8080:

```shell
docker run --name migration-analyzer -p 8080:8080 ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
```

4. Access the web application: http://localhost:8080/


### CLI

The CLI is technically a single jar Java application. 

1. [Download camunda-7-to-8-migration-analyzer-cli.jar](https://artifacts.camunda.com/artifactory/camunda-bpm-community-extensions/org/camunda/community/migration/camunda-7-to-8-migration-analyzer-cli/  TODO  )
2. Start CLI application:

```shell
java -jar backend-diagram-converter-cli-TODO.jar --help
```

## Building from source

You can checkout this project and build from sources:

```shell
mvn clean package
```
