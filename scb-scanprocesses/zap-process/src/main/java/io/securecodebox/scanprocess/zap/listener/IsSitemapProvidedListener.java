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
package io.securecodebox.scanprocess.zap.listener;

import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.execution.Target;
import io.securecodebox.scanprocess.zap.constants.ZapProcessVariables;
import io.securecodebox.scanprocess.zap.constants.ZapTargetAttributes;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IsSitemapProvidedListener implements ExecutionListener {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IsSitemapProvidedListener.class);

    @Autowired
    ScanProcessExecutionFactory processExecutionFactory;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        LOG.debug("Check if all Targets provide a sitemap");
        ScanProcessExecution scanProcess = processExecutionFactory.get(execution);
        List<Target> targets = scanProcess.getTargets();

        boolean allTargetsHaveSitemap = targets.stream()
                .filter(target -> !hasSitemap(target))
                .count() == 0;

        if(allTargetsHaveSitemap){
            LOG.debug("-> All Targets have sitemap. Set SKIP_SPIDER to true");
            execution.setVariable(ZapProcessVariables.SKIP_SPIDER.name(),true);
        } else {
            LOG.debug("-> NOT all Targets have sitemap. Set SKIP_SPIDER to false");
            execution.setVariable(ZapProcessVariables.SKIP_SPIDER.name(),false);
        }
    }

    boolean hasSitemap(Target target){
        return target.getAttributes().containsKey(ZapTargetAttributes.ZAP_SITEMAP.name());
    }

}
