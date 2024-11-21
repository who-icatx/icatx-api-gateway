package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(GetTablePostCoordinationAxisRequest.CHANNEL)
public record GetTablePostCoordinationAxisRequest(
        String entityType) implements Request<GetTablePostCoordinationAxisResponse> {
    public final static String CHANNEL = "webprotege.postcoordination.GetTablePostCoordinationAxis";

    @JsonCreator
    public GetTablePostCoordinationAxisRequest(@JsonProperty("entityType") String entityType) {
        this.entityType = entityType;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
