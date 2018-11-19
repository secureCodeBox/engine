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
package io.securecodebox.engine.service;

import io.securecodebox.engine.model.PermissionType;
import io.securecodebox.engine.model.ResourceType;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationService {

    @Autowired
    ProcessEngine engine;

    public boolean isAuthorizedFor(String resourceId, ResourceType resource, PermissionType permission){
        Authentication auth = engine.getIdentityService().getCurrentAuthentication();
        return engine.getAuthorizationService().isUserAuthorized(
                auth.getUserId(),
                auth.getGroupIds(),
                permission.getCamundaPermission(),
                resource.getCamundaResource(),
                resourceId
        );
    }
}
