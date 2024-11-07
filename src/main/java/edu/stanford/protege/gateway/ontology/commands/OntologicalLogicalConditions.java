package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;

import java.util.List;

public record OntologicalLogicalConditions(
    @JsonProperty("logicalDefinitions") List<LogicalDefinition> logicalDefinitions,
    @JsonProperty("necessaryConditions") List<PropertyClassValue> necessaryConditions
    ){
}
