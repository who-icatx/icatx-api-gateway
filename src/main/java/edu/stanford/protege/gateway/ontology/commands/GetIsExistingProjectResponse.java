package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.ontology.commands.GetIsExistingProjectRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetIsExistingProjectResponse(
        @JsonProperty("isExistingProject") boolean isExistingProject
) implements Response {
}
