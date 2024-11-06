package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityLogicalConditionsWrapper(List<EntityLogicalDefinition> logicalDefinitions, List<LogicalConditionRelationship> necessaryConditions) {
}
