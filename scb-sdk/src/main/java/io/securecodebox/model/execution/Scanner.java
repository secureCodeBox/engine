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

package io.securecodebox.model.execution;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.securecodebox.model.findings.Finding;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 09.03.18
 */
@JsonPropertyOrder({ "id", "type", "findings", "rawFindings" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Scanner {

    @JsonProperty("id")
    protected UUID id;

    @JsonProperty("type")
    protected String type;

    @JsonProperty("findings")
    protected List<Finding> findings;

    @JsonProperty("rawFindings")
    protected String rawFindings;

    public Scanner() {
    }

    public Scanner(UUID id) {
        this();
        this.id = id;
    }

    public Scanner(UUID id, String type) {
        this(id);
        this.type = type;
    }

    public Scanner(UUID id, String type, String rawFindings) {
        this(id, type);
        this.rawFindings = rawFindings;
    }

    public Scanner(UUID id, String type, String rawFindings, List<Finding> findings) {
        this(id, type, rawFindings);
        this.findings = findings;
    }

    public void setScannerId(UUID id) {
        this.id = id;
    }

    @JsonGetter("id")
    public UUID getScannerId() {
        return id;
    }

    public void setScannerType(String type) {
        this.type = type;
    }

    @JsonGetter("type")
    public String getScannerType() {
        return type;
    }

    @JsonGetter("findings")
    public List<Finding> getFindings() {
        return findings != null ? findings : new LinkedList<>();
    }

    /**
     * Clears all {@link Finding}s in this Scanner.
     * <p>
     * After invoking this method, {@link Scanner#getFindings()} will return zero elements.
     */
    @JsonIgnore
    public void clearFindings() {
        findings = new LinkedList<>();
    }

    /**
     * Clears the raw findings in this Scanner.
     * <p>
     * After invoking this method, {@link Scanner#getRawFindings()} ()} will return an empty string.
     */
    @JsonIgnore
    public void clearRawFindings() {
        rawFindings = "";
    }

    /**
     * This are the raw findings from a scanner. They can be in different formats.
     * <p>
     * The raw findings get not persisted in the ScanResult. But can be used to convert it to Findings.
     * <p>
     * For example:
     * - JSON
     * - XML
     * - RAW String Output
     * - LOG Output
     * - Base64
     * - ...
     *
     * @return some String representing the findings in a raw format.
     */
    @JsonIgnore
    public String getRawFindings() {
        return rawFindings;
    }

    /**
     * Appends a finding to the finding list.
     *
     * @param finding
     *
     * @throws IllegalStateException if something goes wrong writing the finding to the process
     */
    public synchronized void appendFinding(Finding finding) {
        findings.add(finding);
    }

    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Scanner scanner = (Scanner) o;
        return Objects.equals(id, scanner.id) && Objects.equals(type, scanner.type) && Objects.equals(findings,
                scanner.findings) && Objects.equals(rawFindings, scanner.rawFindings);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, findings, rawFindings);
    }

}
