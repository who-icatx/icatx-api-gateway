package edu.stanford.protege.gateway.history.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.gateway.dto.ChangedEntities;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.history.commands.GetChangedEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetChangedEntitiesResponse(@JsonProperty("changedEntities") ChangedEntities changedEntities) implements Response {
}
