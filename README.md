  ![Build Status](https://travis-ci.com/secureCodeBox/engine.svg?token=N5PJUt4SAUxNTYFZNtLj&branch=develop)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
  [![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/engine/badge.svg)](https://snyk.io/test/github/secureCodeBox/engine)

 # SecureCodeBox Engine – the Core

This is the main component of the _secureCodeBox_ it's a [Camunda][camunda] [BPMN][bpmn] engine, which allows the engineer to build the whole scan process as a [BPMN][bpmn] model. This component also provides the main web UI: The _secureCodeBox_ control center. In this UI you can see the available scan process definitions as [BPMN][bpmn] diagrams, start them (Tasklist), and manually review the results. Furthermore, the core provides a possibility to listen on webhooks and integrate the exposed process API, allowing us to trigger the scan processes by a continuous integration component, such as [Jenkins][jenkins], in our example, or any other which can deal with webhooks.

 **Important note**: The _secureCodeBox_ is no simple one-button-click-solution! You must have a deep understanding of security and how to configure the scanners. Furthermore, an understanding ot the scan results and how to interpret them is also necessary.

 Further Documentation:
 * [Project Description][scb-project]
 * [Developer Guide][scb-developer-guide]
 * [User Guide][scb-user-guide]

# Configuration Options
To configure the SCB engine specify the following environment variables:

| Environment Variable                  | Description                        | Example Value               |
| ------------------------------------- | ---------------------------------- | --------------------------- |
| SECURECODEBOX_DEFAULT_TARGET_NAME     | Default target identifier          | BodgeIT Public Host         |
| SECURECODEBOX_DEFAULT_TARGET_LOCATION | Default target hostname/ip address | bodgeit                     |
| SECURECODEBOX_DEFAULT_TARGET_URI      | Default target URI/URL             | http://bodgeit:8080/bodgeit |
| SECURECODEBOX_DEFAULT_CONTEXT         | Default business context           | BodgeIT                     |

# Development

## Local setup

1.  Clone the repository
2.  You might need to install some dependencies `java`, `maven`
3.  Run locally `mvn spring-boot:run -Pdev`

## Test

To run the testsuite run:

`mvn test`

## Build

To build the docker container run:

`docker build -t CONTAINER_NAME .`

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
