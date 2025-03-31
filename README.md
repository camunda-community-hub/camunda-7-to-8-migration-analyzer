# Migration Analyzer

The migration analyzer can do tasks for you:

* Analyze Camunda 7 models (BPMN & DMN) to find tasks you would need to do to migrate them to Camunda 8
* Convert those models

The actual convertion can be extended if you have special needs.

## How-to use

You can use the migration analyzer in different ways:

- Local Webapp: Web-based wizard to use the migration analyzer. Refer to the [Installation guide](./webapp/README.md). A Docker image is available.
- CLI: A Command-Line-Interface for the migration analyzer. Refer to the [CLI documentation](./cli/README.md). A library is available.
- SaaS: Access the migration analyzer in a free hosted version: https://diagram-converter.consulting-sandbox.camunda.cloud/. We don't store any of your data.


## How-to build

```shell
mvn clean package
```
