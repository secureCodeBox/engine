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
package io.securecodebox.engine.model;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;

public enum PermissionType {
    NONE(Permissions.NONE),
    ALL(Permissions.ALL),
    READ(Permissions.READ),
    CREATE(Permissions.CREATE),
    CREATE_INSTANCE(Permissions.CREATE_INSTANCE),
    UPDATE(Permissions.UPDATE),
    UPDATE_INSTANCE(Permissions.UPDATE_INSTANCE),
    DELETE(Permissions.DELETE),
    DELETE_INSTANCE(Permissions.DELETE_INSTANCE);

    private Permission permission;

    PermissionType(Permission permission){
        this.permission = permission;
    }

    public Permission getCamundaPermission() {
        return permission;
    }
}
