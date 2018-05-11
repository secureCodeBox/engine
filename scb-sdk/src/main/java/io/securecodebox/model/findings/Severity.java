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

package io.securecodebox.model.findings;

import io.swagger.annotations.ApiModel;

/**
 * Severity of an issue.
 * <p>
 * INFORMATION - Has normally no impact but is a bad behavior.
 * LOW - Small impact, Low Risk. Hard to exploit, hard to find.
 * MEDIUM - Medium impact, medium risk. Either easy to exploit but hard to find or hard to exploit and easy to find.
 * HIFG - Medium to High impact, high risk. Easy to exploit and easy to find.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@ApiModel(description = "Severity of an issue:\n"
        + " * INFORMATION - Has normally no impact but is a bad behavior.\n"
        + " * LOW - Small impact, Low Risk. Hard to exploit, hard to find.\n"
        + " * MEDIUM - Medium impact, medium risk. Either easy to exploit but hard to find or hard to exploit and easy to find.\n"
        + " * HIGH - Medium to High impact, high risk. Easy to exploit and easy to find.")

public enum Severity {
    // The ordinal order is relevant for criticality detection! See Report class for more information.
    // Ordinal order: INFORMATIONAL < LOW < MEDIUM < HIGH
    INFORMATIONAL, LOW, MEDIUM, HIGH;
}
