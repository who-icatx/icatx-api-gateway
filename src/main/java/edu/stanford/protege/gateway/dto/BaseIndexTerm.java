package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaseIndexTerm(@JsonProperty("label") String label,
                            @JsonProperty("indexType") String indexType,
                            @JsonProperty("isInclusion") boolean isInclusion,
                            @JsonProperty("termId") String termId
                            ) {
}
