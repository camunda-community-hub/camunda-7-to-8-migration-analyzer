# Migration Analyzer & Diagram Converter Webapp

## Rest API

`POST /check`: Check required tasks for Camunda 7 to 8 migration for all provided models

- Request:
  - Format: `FormData`
  - Fields
    - `file` (`MultipartFile`): 1..n BPMN file(s) _(mandatory)_
    - `adapterJobType` (`String`): type of the job all service tasks formerly
      implemented as delegates or expressions should have. _(optional)_
    - `platformVersion` (`String`): version of the target platform _(optional)_
    - `adapterEnabled` (`Boolean`): whether the adapter job type should be set in the converted diagram _(default: `true`)_
  - Headers
    - `Accept`: Either `application/json` or `text/csv` or `application/ms-excel`
- Response:
  - `200`: Everything fine. The body contains a
    [check results](../core/src/main/java/org/camunda/community/migration/converter/BpmnDiagramCheckResult.java),
    either in `application/json` format or flattened as `text/csv` or a Microsoft Excel file (XLST).

`POST /convert`: convert the provided model from Camunda 7 to 8

- Request:
  - Format: `FormData`
  - Fields
    - `file` (`MultipartFile`): BPMN file _(mandatory)_
    - `appendDocumentation` (`Boolean`): whether the check results should also
      be added to the documentation of each BPMN element _(default: `false`)_
    - `adapterJobType` (`String`): type of the job all service tasks formerly
      implemented as delegates or expressions should have. _(optional)_
    - `platformVersion` (`String`): version of the target platform _(optional)_
    - `adapterEnabled` (`Boolean`): whether the adapter job type should be set in the converted diagram _(default: `true`)_
- Response:
  - `200`: Everything fine. The body contains a BPMN diagram. The header
    contains a `Content-Disposition` field that declares this as attachment and
    holds a filename. The `Content-Type` is `application/bpmn+xml`.

`POST /convertBatch`: Convert all provided models from Camunda 7 to 8 and return a ZIP file

## Slack Notifications

The app can be configured to notify people in the background via Slack.

To use this feature, you need to configure the following:

```yaml
notification:
  slack:
    enabled: true
    token: <YOUR_BOT_TOKEN>
    channel-name: <NAME_OR_ID_OF_THE_CHANNEL>
```

You can also pass this as environment variables to the Docker run command:

```shell
docker run --name migration-analyzer -p 8080:8080 -e NOTIFICATION_SLACK_ENABLED=true -e NOTIFICATION_SLACK_CHANNEL-NAME=<NAME_OR_ID_OF_THE_CHANNEL> -e NOTIFICATION_SLACK_TOKEN=<YOUR_BOT_TOKEN>  ghcr.io/camunda-community-hub/camunda-7-to-8-migration/migration-analyzer:latest
```

In order to function, the slack app this notification service will be connected
to needs these scopes: `channels:read`, `chat:write`, `chat:write.public` and
`files:write`.

Also, the Bot needs to be added to the channel it should send the notifications
to. Otherwise, no stacktrace files will arrive.
