package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectSummaryDto(@JsonProperty("projectId") String projectId,
                                @JsonProperty("title") String title,
                                @JsonProperty("description") String desciption) {
}
