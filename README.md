# Migration Analyzer

The migration analyzer can do tasks for you:

* Analyze Camunda 7 models (BPMN & DMN) to find tasks you would need to do to migrate them to Camunda 8
* Convert those models

The actual conversion can be extended if you have special needs.

## Installation

You can use the migration analyzer in different ways:

- Web application: Local installation of a web-based wizard to use the migration analyzer. Technically a Java Spring application.
- CLI: A Command-Line-Interface for the migration analyzer. Technically a Java application.
- SaaS: Access the migration analyzer in a free hosted version: https://diagram-converter.consulting-sandbox.camunda.cloud/. We don't store any of your data there!

### Web application

#### Local installation

Requirements:
- Java >= 17

Steps: 

1. Download the latest migration analyzer web application: [camunda-7-to-8-migration-analyzer-webapp.jar])(https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer/releases/latest/camunda-7-to-8-migration-analyzer-webapp.jar)
2. Run the application (point your command line to the directory you downloaded the above jar into): `java -jar camunda-7-to-8-migration-analyzer-webapp.jar`
3. Access the web application: http://localhost:8080/

You can adjust the port if you need to run it on a different port, for example:

```bash
java -Dserver.port=8090 -jar camunda-7-to-8-migration-analyzer-webapp.jar
```

#### Docker

You can also run the migration analyzer via Docker.

Pull the latest version:

```shell
docker pull ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
```

Run the image and expose the port 8080:

```shell
docker run -p 8080:8080 ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
```

Access the web application: http://localhost:8080/


### CLI

The CLI is technically a single jar Java application. 

1. Download the latest migration analyzer CLI application: [camunda-7-to-8-migration-analyzer-cli.jar])(https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer/releases/latest/camunda-7-to-8-migration-analyzer-cli.jar)
2. Start it:

```shell
java -jar camunda-7-to-8-migration-analyzer-cli.jar --help
```

## Building from source

You can build this project from the sources using Maven:

```shell
mvn clean package
```
