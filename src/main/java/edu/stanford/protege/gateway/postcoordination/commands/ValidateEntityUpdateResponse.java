package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.gateway.postcoordination.commands.ValidateEntityUpdateRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public class ValidateEntityUpdateResponse implements Response {

    @JsonProperty("errorMessages")
    private final List<String> errorMessages;

    public ValidateEntityUpdateResponse() {
        this.errorMessages = List.of();
    }

    public ValidateEntityUpdateResponse(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
