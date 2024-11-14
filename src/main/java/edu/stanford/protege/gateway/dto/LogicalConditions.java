package edu.stanford.protege.gateway.dto;

import java.util.List;

public record LogicalConditions(List<EntityLogicalDefinition> logicalDefinitions,
                                List<LogicalConditionRelationship> necessaryConditions) {
}
