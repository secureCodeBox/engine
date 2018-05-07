  ![Build Status](https://travis-ci.com/secureCodeBox/engine.svg?token=N5PJUt4SAUxNTYFZNtLj&branch=develop)
 
 # Process Engine – the Core
 
This is the main component of the _secureCodeBox_ it's a [Camunda][camunda] [BPMN][bpmn] engine, which allows the engineer to build the whole scan process as a [BPMN][bpmn] model. This component also provides the main web UI: The _secureCodeBox_ control center. In this UI you can see the available scan process definitions as [BPMN][bpmn] diagrams, start them (Tasklist), and manually review the results. Furthermore, the core provides a possibility to listen on webhooks and integrate the exposed process API, allowing us to trigger the scan processes by a continuous integration component, such as [Jenkins][jenkins], in our example, or any other which can deal with webhooks.
 
 **Important note**: The _secureCodeBox_ is no simple one-button-click-solution! You must have a deep understanding of security and how to configure the scanners. Furthermore, an understanding ot the scan results and how to interpret them is also necessary.
 
 Further Documentation: 
 * [Project Description][scb-project]
 * [Developer Guide][scb-developer-guide]
 * [User Guide][scb-user-guide]
 
 # Guidelines
 ## Coding Guidelines

### Attributes
Attributes / variables for processes are always wrapped in an enum type.
Attributes that are only used in BPMN files and forms are also named with a prefix and in UPPERCASE.
Common attributes use the prefix `PROCESS`, specific attributes use the technology as a prefix, e.g. `NMAP_TARGET_NAME`.

### JSON
We're using snake_case (lower case) for json attributes. If an enum type is used as attribute its converted to lower case. If it's an value it's always used UPPERCASE. This is to hold the attribute api consistent, but make shure Enums are recognized as enums.

```json
{
    "id": "e18cdc5e-6b49-4346-b623-28a4e878e154",
    "name": "Open mysql Port",
    "description": "Port 3306 is open using tcp protocol.",
    "category": "Open Port",
    "osi_layer": "NETWORK",
    "severity": "INFORMATIONAL",
    "attributes": {
      "protocol": "tcp",
      "port": 3306,
      "service": "mysql",
      "mac_address": null,
      "start": "1520606104",
      "end": "1520606118",
      "ip_address": "127.0.0.1",
      "state": "open"
    },
    "location": "tcp://127.0.0.1:3306"
  }
``` 
### Topic Names for External Tasks
Topics for external tasks for specific technologies are named as follows:
```
$TECHNOLOGY_$TASK
Example: nmap_portscan
```
Topics for tasks that are independent of the used technology are named as follows:
```
task_$TASK
Example: task_mark_false_positive
```

### Naming conventions for git repositories and processes

The scanner repositories are named as follows:
```
scanner-$FUNCTION-$TECHNOLOGY
Example: scanner-infrastructure-nmap
```
The process repositories are named as follows:
```
$TECHNOLOGY-process
Example: nmap-process 
```

### Naming conventions for Process IDs and Names in BPMN Files
Process ids use the following format:
```
$TECHNOLOGY-process[-$DESCRIPTION]
Examples: nmap-process, nmap-process-raw
```

Process names use the following format:
```
$TECHNOLOGY $FUNCTION [- $DESCRIPTION]
Examples: NMAP Port Scan, NMAP Port Scan - Raw
```

[scb-project]:          https://github.com/secureCodeBox/secureCodeBox
[scb-developer-guide]:  https://github.com/secureCodeBox/starter/blob/master/docs/developer-guide/README.md
[scb-user-guide]:       https://github.com/secureCodeBox/starter/tree/master/docs/user-guide

[camunda]:              https://camunda.com/de/
[bpmn]:                 https://en.wikipedia.org/wiki/Business_Process_Model_and_Notation
[jenkins]:              https://jenkins.io/

[docker]:               https://www.docker.com/
[beta-testers]:         https://www.securecodebox.io/
[owasp]:                https://www.owasp.org/index.php/Main_Page
