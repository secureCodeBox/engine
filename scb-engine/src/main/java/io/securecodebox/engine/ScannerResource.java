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

package io.securecodebox.engine;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 16.04.18
 */
@RestController
@RequestMapping(value = "/box/scanner")
public class ScannerResource {

    @Autowired
    ProcessEngine engine;

    @RequestMapping(method = RequestMethod.POST, value = "/jobs/lock/{topic}")
    public String lockJob(@PathVariable String topic) {

        return "KEKS " + engine.getName() + "!";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/jobs")
    public String getJobs() {

        return "KEKS " + engine.getName() + "!";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/jobs/{id}")
    public String completeJob(@PathVariable UUID id, @RequestBody String keks) {

        return "KEKS " + engine.getName() + "!";
    }
}
