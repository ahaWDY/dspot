# DSpot

[![Build Status](https://travis-ci.org/STAMP-project/dspot.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot) [![Coverage Status](https://coveralls.io/repos/github/STAMP-project/dspot/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/dspot?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/dspot/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/dspot)

## Getting started

### Prerequisites

You need Java and Maven.

DSpot uses the environment variable MAVEN_HOME, ensure that this variable points to your maven installation. Example:
```
export MAVEN_HOME=path/to/maven/
```

DSpot uses maven to compile, and build the classpath of your project. The environment variable JAVA_HOME must point to a valid JDK installation (and not a JRE).

### Compilation

1) Clone the project:
```
git clone https://github.com/STAMP-project/dspot.git
cd dspot/dspot
```

2) Compile DSpot
```
mvn compile
```

3) Run the tests
```
mvn test
```

4) Create the jar (_e.g._ `target/dspot-1.0.0-jar-with-dependencies.jar`)
```
mvn package
# check that this is successful
ls target/dspot-*-jar-with-dependencies.jar
```

5) Run the jar
```
java -cp target/dspot-*-jar-with-dependencies.jar eu.stamp_project.Main -p path/To/my.properties
```

For more info, see section **Usage** below.

### Contributing

We accept contribution in form of pull requests. Pull requests must include at least on test that verify the changes.

For each pull request opened, travis is triggered. Our CI contains different jobs that must all pass.

There are jobs that execute the test for the different module of DSpot: `DSpot Core`, `DSpot Maven plugin`, `DSpot diff test selection`, and `DSpot prettifier`.

There are also jobs for different kind of execution: from command line, using the maven plugin from command line and from a configuration in the pom, on large and complex code base.

We use a checkstyle to ensure a minimal code readability.

The code coverage (instruction level) must not decrease by 1% and under 80%.  

### Using DSpot as an API

In this section, we explain the API of **DSpot**. To amplify your tests with **DSpot** you must do 3 steps:
First of all, you have to create an `InputConfiguration`. Only the path to your _properties_ is required:

```java
// 1. Instantiate `InputConfiguration` and `InputProgram`
String propertiesFilePath = <pathToYourPropertiesFile>;
InputConfiguration userInput = new InputConfiguration(propertiesFilePath);
```

Then you have to build the `InputProgram`, this is done by attaching the `InputProgram` to your `InputConfiguration`:

```java
InputProgram program = new InputProgram();
userInput.setInputProgram(program);
```
Then, you are ready to construct the `DSpot` object that will allow you to amplify your test.
There are a lot of constructor available, all of them allow you to custom your `DSpot` object, and so your amplification.
Following the shortest constructor with all default values of `DSpot`, and the longest, which allows to custom all values of `DSpot`:

```java
// 2. Instantiate `DSpot` object
DSpot dspot = new DSpot(InputConfiguration);
DSpot dspot = new DSpot(
    InputConfiguration userInput, // input configuration built at step 1
    int numberOfIterations, // number of time that the main loop will be applied (-i | --iteration option of the CLI)
    List<Amplifier> amplifiers, // list of the amplifiers to be used (-a | --amplifiers option of the CLI)
    TestSelector testSelector // test selector criterion (-s | --test-selector option of the CLI)
);
```

Now that you have your `DSpot`, you will be able to amplify your tests.
`DSpot` has several methods to amplify, but all of them starts with amplify key-word:

```java
// 3. start ampification
dspot.amplifyTest(String regex); // will amplify all test classes that match the given regex
dspot.amplifyTest(String fulQualifiedName, List<String> testCasesName); // will amplify test cases that have their name in testCasesName in the test class fulQualifiedName
dspot.amplifyAllTests(); // will amplify all test in the test suite.
```

## Using DSpot-web

## Directories

Projects being analysed by the DSpot Web UI are cloned and analysed in a dedicated directory, which can be configured in section `work_dir` of the main configuration file of the application: `d_spot_web.conf`.

In this directory each directory is a project, with all required files and information for the project stored inside. A typical architecture looks as follows:

* `work_dir`
  - `project_a`
    - `src` is the git extract (clone) of the repository.
    - `output` is the hierarchy generated during the dspot execution.
    - `results.zip` is the compressed file of the results.
    - `logs` contains the logs of all major actions:
      - `dspot.log` is the log of the dspot execution.
      - `git_clone.log` is the log of the `git clone` command.
      - `git_pull.log` is the log of the last `git pull` command, if relevant.

Please refer to [the dedicated README](dspot-web/v2-mojo/README.md) for information about how to install, configure, start and stop the application.


## Contributing

DSpot is licensed under LGPLv3. Contributors and pull requests are welcome.
