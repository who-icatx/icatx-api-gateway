package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaseExclusionTerm(@JsonProperty("label") String label,
                                @JsonProperty("foundationReference") String foundationReference,
                                @JsonProperty("termId") String termId) {
}
