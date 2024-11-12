package edu.stanford.protege.gateway.dto;

import java.util.List;

public record ChangedEntities(List<String> createdEntities,
                              List<String> updatedEntities,
                              List<String> deletedEntities) {
}
