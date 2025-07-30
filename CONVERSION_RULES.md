# Technical details

Technical details around the differences between Camunda 7 and Camunda 8 BPMN and DMN models. Basis for the logic of the Migration Analyzer & Diagram Converter.

**Warning:** Sometimes these docs are not updated when the code changes. The truth is always in the source code - and we apologize if you find inconsistencies.

## BPMN models

Changes to migrate BPMN models from Camunda 7 to Camunda 8:

- The namespace of extensions has changed from `http://camunda.org/schema/1.0/bpmn` to `http://camunda.org/schema/zeebe/1.0`.
- `modeler:executionPlatform` has been set to `Camunda Cloud`. Prior to this change, you will see `Camunda Platform`, indicating designed compatibility with Camunda 7.
- `modeler:executionPlatformVersion` has been set to `8.8.0`. Prior to this change, you will see `7.19.0` or similar.
- Different configuration attributes are used between platform versions, as described for each BPMN element below.
- Camunda 8 has a _different coverage_ of BPMN elements (see [Camunda 8 BPMN coverage](/components/modeler/bpmn/bpmn-coverage.md) versus [Camunda 7 BPMN coverage](https://docs.camunda.org/manual/latest/reference/bpmn20/)), which might require some model changes. Note that the coverage of Camunda 8 will increase over time.

:::info
Web Modeler will automatically update `modeler:executionPlatform` and `modeler:executionPlatformVersion` to the correct values when you upload a BPMN file.
:::

The following sections describe the capabilities of the existing [diagram converter](https://github.com/camunda-community-hub/camunda-7-to-8-migration/tree/main/backend-diagram-converter) for relevant BPMN symbols, including unsupported element attributes that cannot be migrated.

### General considerations

The following attributes/elements **cannot** be migrated:

- `camunda:asyncBefore`: Every task in Zeebe is always asyncBefore and asyncAfter.
- `camunda:asyncAfter`: Every task in Zeebe is always asyncBefore and asyncAfter.
- `camunda:exclusive`: Jobs are always exclusive in Zeebe.
- `camunda:jobPriority`: There is no way to prioritize jobs in Zeebe (yet).
- `camunda:failedJobRetryTimeCycle`: You cannot yet configure the retry time cycle, only the configured retries will be taken into account. Alternatively, you can [modify your code](/apis-tools/zeebe-api/gateway-service.md#input-failjobrequest) to use the `retryBackOff` timeout (in ms) for the next retry.

### Processes

The following attribute can be migrated:

- `camunda:versionTag` to `bpmn:extensionElements > zeebe:versionTag value`.
