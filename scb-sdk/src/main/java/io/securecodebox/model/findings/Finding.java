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
@JsonPropertyOrder(
        { "id", "name", "description", "category", "osiLayer", "serverity", "reference", "hint", "attributes",
                "location" })
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Finding {

    /**
     * Id of the finding. Must be unique for every finding.
     */
    @JsonProperty("id")
    private UUID id;
    private String name;
    private String description;

    private String category;
    @JsonProperty(value = "osi_layer", required = false)
    private OsiLayer osiLayer;
    private Severity serverity;

    private Reference reference;

    /**
     * An additional solution hint for a finding found. For example SQL-Injection: Please think about using prepared statements.
     */
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
    private String location;

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

    public Severity getServerity() {
        return serverity;
    }

    public void setServerity(Severity serverity) {
        this.serverity = serverity;
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
                && serverity == finding.serverity && Objects.equals(reference, finding.reference) && Objects.equals(
                hint, finding.hint) && Objects.equals(attributes, finding.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, osiLayer, serverity, reference, hint, attributes);
    }
}
