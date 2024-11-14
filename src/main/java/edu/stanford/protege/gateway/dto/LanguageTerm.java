package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LanguageTerm(@JsonProperty("label") String label, @JsonProperty("termId") String termId) {
}
