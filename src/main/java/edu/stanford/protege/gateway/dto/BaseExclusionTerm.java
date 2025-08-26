package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record BaseExclusionTerm(@JsonProperty("label") String label,
                                @NotBlank @JsonProperty("foundationReference") String foundationReference,
                                @JsonProperty("termId") String termId) {
}
