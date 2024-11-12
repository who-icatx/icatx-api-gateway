package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.linearization.commands.SaveEntityLinearizationRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record SaveEntityLinearizationResponse() implements Response {
    public static SaveEntityLinearizationResponse create() {
        return new SaveEntityLinearizationResponse();
    }
}
