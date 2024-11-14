package edu.stanford.protege.gateway.history.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import java.sql.Timestamp;

import static edu.stanford.protege.gateway.history.commands.GetChangedEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetChangedEntitiesRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("timestamp") Timestamp timestamp
) implements Request<GetChangedEntitiesResponse> {

    public static final String CHANNEL = "webprotege.history.GetChangedEntities";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static GetChangedEntitiesRequest create(ProjectId projectId,
                                                   Timestamp timestamp) {
        return new GetChangedEntitiesRequest(projectId, timestamp);
    }
}
