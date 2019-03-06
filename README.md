[![Build Status](https://travis-ci.com/secureCodeBox/engine.svg?branch=develop)](https://travis-ci.com/secureCodeBox/engine)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/engine/badge.svg)](https://snyk.io/test/github/secureCodeBox/engine)
[![GitHub release](https://img.shields.io/github/release/secureCodeBox/engine.svg)](https://github.com/secureCodeBox/engine/releases/latest)

 # SecureCodeBox Engine – the Core

This is the main component of the _secureCodeBox_ it's a [Camunda][camunda] [BPMN][bpmn] engine, which allows the engineer to build the whole scan process as a [BPMN][bpmn] model. This component also provides the main web UI: The _secureCodeBox_ control center. In this UI you can see the available scan process definitions as [BPMN][bpmn] diagrams, start them (Tasklist), and manually review the results. Furthermore, the core provides a possibility to listen on webhooks and integrate the exposed process API, allowing us to trigger the scan processes by a continuous integration component, such as [Jenkins][jenkins], in our example, or any other which can deal with webhooks.

 **Important note**: The _secureCodeBox_ is no simple one-button-click-solution! You must have a deep understanding of security and how to configure the scanners. Furthermore, an understanding ot the scan results and how to interpret them is also necessary.

 Further Documentation:
 * [Project Description][scb-project]
 * [Developer Guide][scb-developer-guide]
 * [User Guide][scb-user-guide]

# Configuration Options
To configure the SCB engine specify the following environment variables:

| Environment Variable                  | Description                           | Example Value               |
| ------------------------------------- | ------------------------------------- | --------------------------- |
| SECURECODEBOX_DEFAULT_TARGET_NAME     | Default target identifier             | BodgeIT Public Host         |
| SECURECODEBOX_DEFAULT_TARGET_LOCATION | Default target hostname/ip address    | bodgeit                     |
| SECURECODEBOX_DEFAULT_TARGET_URI      | Default target URI/URL                | http://bodgeit:8080/bodgeit |
| SECURECODEBOX_DEFAULT_CONTEXT         | Default business context              | BodgeIT                     |
| SECURECODEBOX_USER_SCANNER            | Default user for scanner services     | default-scanner             |
| SECURECODEBOX_USER_SCANNER_PW         | Default password for scanner services | AStrongPassword-NotThisOne! |

## Server Configuration
Additionally all properties defined in scb-engine/src/main/resources/application.yaml can be overwritten via environment variables.
This allows you to e.g. enable https using:

| Environment Variable                  | Description                           | Example Value               |
| ------------------------------------- | ------------------------------------- | --------------------------- |
| SERVER_PORT                           | Defines the server port               | 8443                        |
| SERVER_SSL_ENABLED                    | Enables http over ssl                 | true                        |
| SERVER_SSL_KEY_STORE_PASSWORD         | Password to the java keystore         | AStrongPassword-NotThisOne! |

# Development

## Local setup

1.  Clone the repository
2.  You might need to install some dependencies `java`, `maven`
3.  Run locally `mvn spring-boot:run -Pdev`

## Test

To run the testsuite run:

`mvn test`

## Build

To build the docker image run:

`docker build -t IMAGE_NAME .`

## Generating the API Docs

1. Run the Test Suite using the `docs` maven profile. This should generate a `swagger.json` file in the target folder of the `scb-engine` module.
2. Run the `swagger2markup:convertSwagger2markup` plugin. This should generate a file located `docs/api-doc.md` in the target folder of the `scb-engine module.
3. Copy the `api-doc.md` file to the user guide of the [secureCodeBox](https://github.com/secureCodeBox/secureCodeBox) repository.
4. Re Add the first disclaimer paragraph pointing the users to the dynamic swagger docs of their engine. This has to be added by hand as it is not included in the export.
5. (Optional) Reformat the generated markdown file with prettier to improve the generated markdown output.

# Guidelines & Standards
Well boring yes - but please read our [guidelines and naming standards][scb-developer-guidelines].

[scb-project]:              https://github.com/secureCodeBox/secureCodeBox
[scb-developer-guide]:      https://github.com/secureCodeBox/secureCodeBox/blob/develop/docs/developer-guide/README.md
[scb-developer-guidelines]: https://github.com/secureCodeBox/secureCodeBox/blob/develop/docs/developer-guide/README.md#guidelines
[scb-user-guide]:           https://github.com/secureCodeBox/secureCodeBox/tree/develop/docs/user-guide

[camunda]:                  https://camunda.com/de/
[bpmn]:                     https://en.wikipedia.org/wiki/Business_Process_Model_and_Notation
[jenkins]:                  https://jenkins.io/

[docker]:                   https://www.docker.com/
[beta-testers]:             https://www.securecodebox.io/
[owasp]:                    https://www.owasp.org/index.php/Main_Page
