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
package io.securecodebox.engine.tenancy;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.engine.auth.InsufficientAuthorizationException;
import io.securecodebox.engine.service.AuthService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomTenantIdProvider implements TenantIdProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CustomTenantIdProvider.class);

    CustomTenantIdProvider (){
        super();
        LOG.debug("Init CustomTenantIdProvider");
    }

    @Override
    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
        return getTenantIdOfCurrentAuthentication(ctx.getVariables());
    }

    @Override
    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
        return getTenantIdOfCurrentAuthentication(ctx.getVariables());
    }

    @Override
    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
        return getTenantIdOfCurrentAuthentication(ctx.getExecution().getVariablesTyped());
    }

    protected String getTenantIdOfCurrentAuthentication(VariableMap variableMap) {
        LOG.debug("Determining if process should be started with a tenant.");

        // Process doesn't have tenant variable -> was most likely started via camunda ui
        if(!variableMap.containsKey(DefaultFields.PROCESS_TENANT.name())){
            LOG.debug("Process started without tenant variable -> Process will not be associated with a tenant");
            return null;
        }

        String specifiedTenant = variableMap.getValue(DefaultFields.PROCESS_TENANT.name(), String.class);

        // Tenant Id was left empty or was explicitly set to null -> start without tenant
        if(specifiedTenant ==  null || specifiedTenant.equals("")){
            LOG.debug("Tenant field in target was not specified or set to null -> Process will not be associated with a tenant");
            return null;
        }

        AuthService authService = new AuthService(Context.getProcessEngineConfiguration().getProcessEngine());
        Authentication currentAuthentication = authService.getAuthentication();

        if (currentAuthentication == null) {
            throw new InsufficientAuthorizationException("No authenticated user");
        }

        boolean userIsMemberOfTenant = currentAuthentication.getTenantIds().stream().anyMatch(tenant -> tenant.equals(specifiedTenant));

        if(userIsMemberOfTenant){
            LOG.debug("Process started with tenant and user is member of tenant -> Process will be started with tenant '{}'", specifiedTenant);
            return specifiedTenant;
        } else {
            LOG.debug("Process started with tenant, BUT user is NOT a member of the tenant -> Process will crash");
            throw new InsufficientAuthorizationException("User is not a member of the specified Tenant");
        }
    }

}
