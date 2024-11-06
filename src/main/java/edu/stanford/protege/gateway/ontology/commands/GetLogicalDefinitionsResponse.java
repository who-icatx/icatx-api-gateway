package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;

import java.util.Date;
import java.util.List;

@JsonTypeName(GetLogicalDefinitionsRequest.CHANNEL)
public record GetLogicalDefinitionsResponse(List<LogicalDefinition> logicalDefinitions,
                                            @JsonProperty("lastRevisionDate")
                                            Date lastRevisionDate,
                                            List<PropertyClassValue> necessaryConditions) implements Response {
}
