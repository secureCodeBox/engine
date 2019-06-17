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

package io.securecodebox.engine.listener;

import io.securecodebox.engine.tenancy.CustomTenantIdProvider;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 13.04.18
 */
@Component
public class ListenerRegistrarPlugin extends AbstractProcessEnginePlugin {

    @Autowired
    DefaultListenerRegistrar registrar;

    @Autowired
    CustomTenantIdProvider tenantIdProvider;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

        processEngineConfiguration.setTenantIdProvider(tenantIdProvider);

        // get all existing preParseListeners
        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();

        if (preParseListeners == null) {

            // if no preParseListener exists, create new list
            preParseListeners = new ArrayList<BpmnParseListener>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }

        // add new BPMN Parse Listener
        preParseListeners.add(registrar);
    }
}
