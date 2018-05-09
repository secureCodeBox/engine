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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Reference to further details.
 * This can be a reference to Common Vulnerabilities and Exposures, node security, owasp or other
 * <p>
 * CVE Example:
 * id: CVE-2017-15707
 * source: https://www.cvedetails.com/cve/CVE-2017-15707/
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@JsonPropertyOrder({ "id", "source" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Reference {

    @Max(4000)
    @Pattern(regexp = "^[^<>\\\\/\\[\\]()%$]*$")
    private String id;
    @Pattern(regexp = "^[^<>\\\\\\[\\]()%$]*$")
    private String source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Reference reference = (Reference) o;
        return Objects.equals(id, reference.id) && Objects.equals(source, reference.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source);
    }
}
