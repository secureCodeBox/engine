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

package io.securecodebox.engine.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.scanprocess.ProcessVariableHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This class is mainly to inject the Spring {@link ObjectMapper} into the {@link ProcessVariableHelper}!
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 27.04.18
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@Component
public class ProcessVariableHelperInstancer extends ProcessVariableHelper {

    @Autowired
    ProcessVariableHelperInstancer(ObjectMapper objectMapper) {
        ProcessVariableHelper.objectMapper = objectMapper;
    }
}
