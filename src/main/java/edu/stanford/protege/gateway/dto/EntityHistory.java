package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityHistory(List<EntityChange> changes) {
    public static EntityHistory create(List<EntityChange> changes) {
        return new EntityHistory(changes);
    }
}
