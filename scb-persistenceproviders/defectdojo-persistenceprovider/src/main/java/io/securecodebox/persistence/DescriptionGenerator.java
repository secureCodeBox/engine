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

import io.securecodebox.model.securitytest.SecurityTest;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DescriptionGenerator {
    public String generate(SecurityTest securityTest){
        return MessageFormat.format("#{0}  \nTime: {1}  \nTarget: {2} \"{3}\"",
                getDefectDojoScanName(securityTest),
                currentTime(),
                securityTest.getTarget().getName(),
                securityTest.getTarget().getLocation()
        );
    }

    protected static final String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

    Clock clock = Clock.systemDefaultZone();

    private String currentTime() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    private String getDefectDojoScanName(SecurityTest securityTest){
        try{
            return DefectDojoPersistenceProvider.getDefectDojoScanName(securityTest.getName());
        }
        catch(DefectDojoPersistenceException e){
            return securityTest.getName();
        }
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
