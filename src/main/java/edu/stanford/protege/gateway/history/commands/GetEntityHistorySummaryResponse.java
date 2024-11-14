package edu.stanford.protege.gateway.history.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.gateway.history.commands.GetEntityHistorySummaryRequest.CHANNEL;
@JsonTypeName(CHANNEL)
public record GetEntityHistorySummaryResponse(
        @JsonProperty("projectChangesForEntity") List<ProjectChange> projectChangesForEntity
) implements Response {
}
