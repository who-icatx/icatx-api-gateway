package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(LinearizationDefinitionRequest.CHANNEL)
public record LinearizationDefinitionRequest() implements Request<LinearizationDefinitionResponse> {

    public static final String CHANNEL = "webprotege.linearization.GetLinearizationDefinitions";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
