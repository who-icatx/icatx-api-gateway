package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityChildren(
        @JsonProperty("entityUri") String entityUri,
        @JsonProperty("projectId") String projectId,
        @JsonProperty("children") List<String> children
) {
    public static EntityChildren create(
            String entityUri,
            String projectId,
            List<String> children) {
        return new EntityChildren(entityUri, projectId, children);
    }
}
