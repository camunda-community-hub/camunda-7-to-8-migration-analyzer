# Conversion CLI Tool

The command line interface tool can convert a Camunda Platform 7 diagram or all
diagrams inside a folder (plus sub directories) or convert all diagrams from a
Camunda Platform 7 process engine.

All diagrams that should be converted need to have the `.bpmn` or `.bpmn20.xml`
file ending.

The tool logs all results (either location of the new file or the exception the
migration tool faced during migration). Diagrams that are already in C8 format
will be omitted but logged. So you can follow the progress of converting.

In case you run the CLI tool on your Windows computer and your process diagram
contains Umlaute, add the java option `-Dfile.encoding=UTF-8` between the java
and -jar parameters:

```
java -Dfile.encoding=UTF-8 -jar backend-diagram-converter-cli-version.jar
```

## Commands

### Convert diagrams from the local file systems

```
Usage: backend-diagram-converter-cli local [-dhoV] [--check] [--csv]
       [--disable-default-job-type] [-nr] [--default-job-type=<defaultJobType>]
       [--platform-version=<platformVersion>] [--prefix=<prefix>] <file>
Converts the diagram from the given directory or file

Execute as:

java -Dfile.encoding=UTF-8 -jar backend-diagram-converter-cli.jar local

Parameter:
      <file>                 The file to convert or directory to search in
Options:
      --check                If enabled, no converted diagrams are exported
      --csv                  If enabled, a CSV file will be created containing
                               the results for all conversions
  -d, --documentation        If enabled, messages are also appended to
                               documentation
      --default-job-type=<defaultJobType>
                             If set, the default value from the
                               'converter-properties.properties' for the job
                               type is overridden
      --disable-default-job-type
                             Disables the default job type
  -h, --help                 Show this help message and exit.
      -nr, --not-recursive   If enabled, recursive search in subfolders will be
                               omitted
  -o, --override             If enabled, existing files are overridden
      --platform-version=<platformVersion>
                             Semantic version of the target platform, defaults
                               to latest version
      --prefix=<prefix>      Prefix for the name of the generated file
                               Default: converted-c8-
  -V, --version              Print version information and exit.
```

**Example:**

```
java -Dfile.encoding=UTF-8 -jar backend-diagram-converter-cli-v.v.v.jar local c:\myDirectory
```

### Convert diagrams from a running process engine

```
Usage: backend-diagram-converter-cli engine [-dhoV] [--check] [--csv]
       [--disable-default-job-type] [--default-job-type=<defaultJobType>]
       [-p=<password>] [--platform-version=<platformVersion>]
       [--prefix=<prefix>] [-t=<targetDirectory>] [-u=<username>] <url>
Description: Converts the diagrams from the given process engine

Execute as:

java -Dfile.encoding=UTF-8 -jar backend-diagram-converter-cli.jar engine

Parameter:
      <url>               Fully qualified http(s) address to the process engine
                            REST API
                            Default: http://localhost:8080/engine-rest
Options:
      --check             If enabled, no converted diagrams are exported
      --csv               If enabled, a CSV file will be created containing the
                            results for all conversions
  -d, --documentation     If enabled, messages are also appended to
                            documentation
      --default-job-type=<defaultJobType>
                          If set, the default value from the
                            'converter-properties.properties' for the job type
                            is overridden
      --disable-default-job-type
                          Disables the default job type
  -h, --help              Show this help message and exit.
  -o, --override          If enabled, existing files are overridden
  -p, --password=<password>
                          Password for basic auth
      --platform-version=<platformVersion>
                          Semantic version of the target platform, defaults to
                            latest version
      --prefix=<prefix>   Prefix for the name of the generated file
                            Default: converted-c8-
  -t, --target-directory=<targetDirectory>
                          The directory to save the .bpmn files
                            Default: .
  -u, --username=<username>
                          Username for basic auth
  -V, --version           Print version information and exit.
```

**Example:**

```
java -Dfile.encoding=UTF-8 -jar backend-diagram-converter-cli-v.v.v.jar engine http://localhost:8080/engine-rest
```
