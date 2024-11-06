package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;


@JsonTypeName(GetEntityCustomScaleValuesRequest.CHANNEL)
public record GetEntityCustomScaleValuesRequest(@JsonProperty("entityIRI") String entityIRI,
                                                @JsonProperty("projectId") ProjectId projectId) implements Request<GetEntityCustomScaleValueResponse> {

    public static final String CHANNEL = "webprotege.postcoordination.GetEntityScaleValues";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
