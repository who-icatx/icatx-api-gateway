package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.*;

public record BaseIndexTerm(@JsonProperty("label") String label,
                            @JsonProperty("indexType") String indexType,
                            @JsonProperty("isInclusion") boolean isInclusion,
                            @JsonProperty("isDeprecated") @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean isDeprecated,
                            @JsonProperty("termId") String termId
                            ) {
}
