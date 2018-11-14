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
package io.securecodebox.persistence;

import io.securecodebox.model.rest.Report;
import io.securecodebox.model.securitytest.SecurityTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "none")
public class EmptyPersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(EmptyPersistenceProvider.class);

    @Override
    public void persist(SecurityTest securityTest) {

        if (securityTest == null) {
            LOG.warn("SecurityTest is null, nothing to persist.");
        } else {
            LOG.warn(
                    "This SecurityTest will not be persisted, because you have no persistence provider configured in your application.yml");
        }

        return;
    }
}
