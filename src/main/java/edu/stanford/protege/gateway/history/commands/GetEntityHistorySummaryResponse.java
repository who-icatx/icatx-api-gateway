package edu.stanford.protege.gateway.history.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.gateway.dto.EntityHistorySummary;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.history.commands.GetEntityHistorySummaryRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityHistorySummaryResponse(
        @JsonProperty("entityHistorySummary") EntityHistorySummary entityHistorySummary
) implements Response {
}
