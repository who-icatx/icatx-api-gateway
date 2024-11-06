package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityLogicalDefinition(String logicalDefinitionSuperclass, List<LogicalConditionRelationship> relationships ) {
}
