# Extension Example

This module shows how to build a **custom extension** for the [Camunda 7 to 8 Migration Analyzer](https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer).

It shows how to **inspect BPMN DOM elements**, **enqueue targeted conversions**, and **emit analysis messages**.

## What this example does

### 1) Exclusive Gateway: preserve original conditional expressions

**Class:** `CustomDomElementVisitor`

When the visitor encounters an **exclusiveGateway** that **forks** (i.e., it has more than one outgoing sequence flow), it:

- Collects each outgoing sequence flow’s `<bpmn:conditionExpression>` (ID, language, expression).
- Joins them into a single string, e.g.:  
  `Flow_1: juel: ${x > 5}, Flow_2: juel: ${x <= 5}`
- Schedules a conversion on the **exclusive gateway** to **add a Zeebe property**:
  - Key: `originalExpressions`
  - Value: the comma-separated list above
- Emits an **INFO** message:  
  `Original expressions are: <joined list>`

This preserves the original gateway logic for review after conversion.

**Before (simplified)**

```xml
<bpmn:exclusiveGateway id="Gateway_1">
  <bpmn:outgoing>Flow_1</bpmn:outgoing>
  <bpmn:outgoing>Flow_2</bpmn:outgoing>
</bpmn:exclusiveGateway>

<bpmn:sequenceFlow id="Flow_1" sourceRef="Gateway_1" targetRef="Task_A">
  <bpmn:conditionExpression language="juel">${x > 5}</bpmn:conditionExpression>
</bpmn:sequenceFlow>

<bpmn:sequenceFlow id="Flow_2" sourceRef="Gateway_1" targetRef="Task_B">
  <bpmn:conditionExpression>${x <= 5}</bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

If no expression language is set, it is treated as "juel".

**After (illustrative)**

```xml
<bpmn:exclusiveGateway id="Gateway_1">
  <bpmn:extensionElements>
    <zeebe:properties>
      <zeebe:property key="originalExpressions"
        value="Flow_1: juel: ${x > 5}, Flow_2: juel: ${x <= 5}" />
    </zeebe:properties>
  </bpmn:extensionElements>
  <bpmn:outgoing>Flow_1</bpmn:outgoing>
  <bpmn:outgoing>Flow_2</bpmn:outgoing>
</bpmn:exclusiveGateway>
```

### 2) Service Task: migrate topic to Zeebe and set a generic type

**Class**: `TaskTopicVisitor` (extends AbstractSupportedAttributeVisitor)

When a Service Task has a supported attribute named topic:
* Adds a Zeebe task header: key="topic" and value="<topic value>"
* Sets the Zeebe taskDefinition type to "GenericWorker"
* Emits an INFO message: Tasktopic has been transformed: <topic value>

**Before (simplified)**

```xml
<bpmn:serviceTask id="Task_1" camunda:topic="email-sender" />
```

**After (key effect, illustrative)**

```xml
<bpmn:serviceTask id="Task_1">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="GenericWorker" />
    <zeebe:taskHeaders>
      <zeebe:header key="topic" value="email-sender" />
    </zeebe:taskHeaders>
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

