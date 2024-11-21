package edu.stanford.protege.gateway.history.commands;

import java.util.List;

public record EntityHistorySummary(List<EntityChange> changes) {
    public static EntityHistorySummary create(List<EntityChange> changes) {
        return new EntityHistorySummary(changes);
    }
}
