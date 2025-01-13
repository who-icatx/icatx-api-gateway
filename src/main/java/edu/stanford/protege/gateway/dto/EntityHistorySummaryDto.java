package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityHistorySummaryDto(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("entityUri") String entityUri,
        @JsonProperty("changes") List<EntityChange> changes
) {
    public static EntityHistorySummaryDto create(String entityUri,
                                                 String projectId,
                                                 List<EntityChange> changes) {
        return new EntityHistorySummaryDto(entityUri, projectId, changes);
    }
}
