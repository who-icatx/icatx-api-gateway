package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(GetEntityPostCoordinationRequest.CHANNEL)
public record GetEntityPostCoordinationRequest(@JsonProperty("entityIRI") String entityIRI,
                                               @JsonProperty("projectId") ProjectId projectId) implements Request<GetEntityPostCoordinationResponse> {

    public static final String CHANNEL = "webprotege.postcoordination.GetEntityPostCoordinations";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}