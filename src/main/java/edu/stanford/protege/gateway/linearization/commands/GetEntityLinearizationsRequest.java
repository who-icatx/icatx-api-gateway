package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;


@JsonTypeName(GetEntityLinearizationsRequest.CHANNEL)
public record GetEntityLinearizationsRequest(@JsonProperty("entityIRI") String entityIRI,
                                             @JsonProperty("projectId") ProjectId projectId) implements Request<GetEntityLinearizationsResponse> {

    public static final String CHANNEL = "webprotege.linearization.GetEntityLinearizations";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
