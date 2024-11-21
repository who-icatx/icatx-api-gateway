package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

@JsonTypeName(UpdateLogicalDefinitionsRequest.CHANNEL)
public record UpdateLogicalDefinitionsResponse() implements Response {
}
