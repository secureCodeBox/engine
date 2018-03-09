/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.constants;

/**
 * @since 01.04.17
 */
@Deprecated
public class CommonConstants {

    // General
    public static final String PROCESS_UUID = "processUuid";
    public static final String MICROSERVICE = "microserviceType";
    public static final String MICROSERVICE_ID = "microserviceId";
    public static final String CONTEXT = "context";
    public static final String TENAND_ID = "tenantId";

    //TODO: Generify
    // Persistence specific (e.g. elasticsearch)
    public static final String DOCUMENT_UUID = "_id";
    public static final String TIMESTAMP = "@timestamp";

    public static final String TARGET_NAME = "target_name";
    public static final String TARGET_URL = "target_url";
    public static final String SCANNER_NAME = "scanner_name";
    public static final String AUTOMATED_RUN = "automatedRun";
    public static final String REPORTING_TYPES = "reporting_types";
    public static final String CURRENT_SCANNER_NAME = "current_scanner_name";

    // SPIDER
    public static final String SPIDER_MICROSERVICE_ID = "spiderMicroserviceId";
    public static final String SPIDER_MICROSERVICE_RUN_ID = "spiderMicroserviceRunId";
    public static final String SPIDER_TARGET ="spiderTargetUrl";
    public static final String SPIDER_TYPE ="spiderType";

    // SCANNER
    public static final String SCANNER_MICROSERVICE_ID = "scannerMicroserviceId";
    public static final String SCANNER_MICROSERVICE_TYPE = "scannerType";
    public static final String SCANNER_RUN_ID = "scannerMicroserviceRunId";
    public static final String SCANNER_REGEX_EXCLUDE = "scannerExcludeRegexes";
    public static final String SCANNER_REGEX_INCLUDE = "scannerIncludeRegexes";
    public static final String SCANNER_TARGET ="scannerTargetUrl";
}
