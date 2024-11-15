package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.gateway.dto.EntityComments;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityCommentsRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityCommentsResponse(EntityComments comments) implements Response {
}
