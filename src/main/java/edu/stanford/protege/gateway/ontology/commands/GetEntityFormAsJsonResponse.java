package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityFormAsJsonRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityFormAsJsonResponse(EntityForm form)  implements Response {
}
