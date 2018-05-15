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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents findings found by a scanner.
 *
 * @author Rüdiger Heins - iteratec GmbH
 * @since 08.03.18
 */
@ApiModel(description="This type represents findings found by a scanner.")
@JsonPropertyOrder({ "id", "name", "description", "category", "osiLayer", "severity", "reference", "hint", "attributes",
        "location" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Finding {

    /**
     * Id of the finding. Must be unique for every finding.
     */
    @NotNull
    @JsonProperty(value = "id", required = true)
    @ApiModelProperty(value = "The id of the finding.", example = "3dd4840c-81ae-4fed-90b5-b3eea3d4c701",
            required = true)
    private UUID id;
    @Max(4000)
    @Pattern(regexp = "^[^<>\\\\/\\[\\]()%$]*$")
    @ApiModelProperty(value = "The name of the finding.", example = "Open Port", required = true)
    private String name;
    @Pattern(regexp = "^[^<>\\\\/\\[\\]()%$]*$")
    @ApiModelProperty(value = "The name of the finding.", example = "The DNS Port is open.")
    private String description;

    @Pattern(regexp = "^[^<>\\\\/\\[\\]()%$]*$")
    @ApiModelProperty(value = "The category of this finding.", example = "Infrastructure", required = true)
    private String category;
    @JsonProperty(value = "osi_layer", required = false)
    @ApiModelProperty(value = "The osi layer of this finding.", example ="NETWORK")
    private OsiLayer osiLayer;
    @ApiModelProperty(value = "The severity of this finding.", example ="HIGH")
    private Severity severity;

    @ApiModelProperty(value = "An additional external Reference.", example ="CVE-2018-1196")
    private Reference reference;

    /**
     * An additional solution hint for a finding found. For example SQL-Injection: Please think about using prepared statements.
     */
    @ApiModelProperty(value = "An additional solution hint for a finding found.", example ="SQL-Injection: Please think about using prepared statements.")
    private String hint;

    /**
     * A location representation where the finding was found.
     * Please always add some specific attributes.
     * <p>
     * For NMAP Example:
     * Location can be: ajp://162.222.1.3:82
     * <p>
     * Attributes: 
     * NMAP_IP: 162.222.1.3
     * NMAP_PORT: 82
     */
    @ApiModelProperty(value = "The location of this finding.", example ="tcp://162.222.1.3:53", required = true)
    private String location;

    @ApiModelProperty(value = "Key value pairs of scanner specific values.", example ="{\"NMAP_PORT\":34, \"NMAP_IP\":\"162.222.1.3\"}", required = false)
    private Map<String, Object> attributes = new HashMap<>();

    public UUID getId() {
        return id;
    }

    /**
     * Adds scanner specific attributes as key value pairs.
     *
     * @param key   the key representation of the object
     * @param value the value object
     */
    public void addAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    /**
     * Adds scanner specific attributes as key value pairs.
     * <p>
     * Convenience method for addAttribute(keyEnum.name(), value)
     *
     * @param key   the enum representation of the key.
     * @param value the value object
     */
    @JsonIgnore
    public void addAttribute(Enum<?> key, Serializable value) {
        addAttribute(key.name().toLowerCase(), value);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @JsonIgnore
    public Object getAttribute(Enum<?> key) {
        return attributes.get(key.name().toLowerCase());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public OsiLayer getOsiLayer() {
        return osiLayer;
    }

    public void setOsiLayer(OsiLayer osiLayer) {
        this.osiLayer = osiLayer;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Finding finding = (Finding) o;
        return Objects.equals(id, finding.id) && Objects.equals(name, finding.name) && Objects.equals(description,
                finding.description) && Objects.equals(category, finding.category) && osiLayer == finding.osiLayer
                && severity == finding.severity && Objects.equals(reference, finding.reference) && Objects.equals(hint,
                finding.hint) && Objects.equals(attributes, finding.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, osiLayer, severity, reference, hint, attributes);
    }

    @Override
    public String toString() {
        return "Finding{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\''
                + ", category='" + category + '\'' + ", osiLayer=" + osiLayer + ", severity=" + severity
                + ", reference=" + reference + ", hint='" + hint + '\'' + ", location='" + location + '\''
                + ", attributes=" + attributes + '}';
    }
}
