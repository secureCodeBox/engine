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

package io.securecodebox.engine.rest;

import io.securecodebox.model.execution.Target;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API / Endpoint for scan processes (camunda processes).
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 23.04.18
 */

@Api(description = "Scan Process Resource", produces = "application/json", consumes = "application/json")
@RestController
@RequestMapping(value = "/box/processes")
public class ScanProcessResource {
    private static final Logger LOG = LoggerFactory.getLogger(ScanProcessResource.class);

    @ApiOperation(value = "Creates a new scan process. (API STUB NOT IMPLEMENTED!)")
    @ApiResponses(
            value = { @ApiResponse(code = 201, message = "Successful delivery of the failure.", response = void.class),
                    @ApiResponse(code = 400, message = "Incomplete or inconsistent Request"),
                    @ApiResponse(code = 500, message = "Unknown technical error occurred.") })

    @RequestMapping(method = RequestMethod.PUT, value = "/{processName}")
    public ResponseEntity createProcess(@PathVariable String processName, @RequestBody List<Target> targets) {

        LOG.debug("Recived scan failure {}", targets);

        //        Map<String, Object> variables = new HashMap<>();
        //        variables.put(DefaultFields.PROCESS_SCANNER_ID.name(), targets.getScannerId().toString());
        //        variables.put(DefaultFields.PROCESS_SCANNER_TYPE.name(), targets.getScannerType());
        //        variables.put(DefaultFields.PROCESS_RAW_FINDINGS.name(), targets.getRawFindings());
        //        synchronized (DefaultFields.PROCESS_FINDINGS) {
        //            try {
        //                ObjectValue objectValue = Variables.objectValue(objectMapper.writeValueAsString(targets.getFindings()))
        //                        .serializationDataFormat(Variables.SerializationDataFormats.JSON)
        //                        .create();
        //                variables.put(DefaultFields.PROCESS_FINDINGS.name(), objectValue);
        //            } catch (JsonProcessingException e) {
        //                LOG.error("Can't write field {} to process!", DefaultFields.PROCESS_FINDINGS, e);
        //                throw new IllegalStateException("Can't write field to process!", e);
        //            }
        //        }
        //
        //        engine.getExternalTaskService().complete(id.toString(), targets.getScannerId().toString(), variables);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

}
