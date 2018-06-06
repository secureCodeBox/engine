package io.securecodebox.persistence.elasticsearch;

import com.fasterxml.jackson.annotation.*;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KibanaData {

    @JsonProperty(value = "_type", required = true)
    private String type;

    @JsonProperty(value = "_index", required = true)
    private String index;

    @JsonProperty(value = "_id", required = true)
    private String id;

    @JsonProperty(value = "_source", required = true)
    private Map<String, Object> source;

    public String getType() {
        return type;
    }

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getSource() {
        return source;
    }
}
