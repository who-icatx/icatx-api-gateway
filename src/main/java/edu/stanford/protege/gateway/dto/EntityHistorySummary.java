package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityHistorySummary(List<EntityChange> changes) {
    public static EntityHistorySummary create(List<EntityChange> changes) {
        return new EntityHistorySummary(changes);
    }
}
