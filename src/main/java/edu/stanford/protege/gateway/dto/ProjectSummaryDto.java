package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectSummaryDto(@JsonProperty("projectId") String projectId,
                                @JsonProperty("title") String title,

                                @JsonProperty("createdAt") long createdAt,
                                @JsonProperty("description") String description,
                                @JsonProperty("gitRepoBranch") String gitRepoBranch) {
}
