package io.securecodebox.persistence.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ToolType {
    @JsonProperty
    String url;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
