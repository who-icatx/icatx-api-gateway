package edu.stanford.protege.gateway.history.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Request;

import static edu.stanford.protege.gateway.history.commands.GetEntityHistorySummaryRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityHistorySummaryRequest(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("entityIri") String entityIri
) implements Request<GetEntityHistorySummaryResponse> {

    public static final String CHANNEL = "webprotege.history.GetEntityHistorySummary";

    public static GetEntityHistorySummaryRequest create(String projectId,
                                                        String entityIri) {
        return new GetEntityHistorySummaryRequest(projectId, entityIri);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
