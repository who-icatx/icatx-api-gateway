package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import javax.annotation.Nonnull;
import java.util.List;

import static edu.stanford.protege.gateway.ontology.commands.ValidateLogicalDefinitionFromApiRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record ValidateLogicalDefinitionFromApiResponse(
        @JsonProperty("messages") @Nonnull List<String> messages
) implements Response {

    public static ValidateLogicalDefinitionFromApiResponse create(@Nonnull List<String> messages) {
        return new ValidateLogicalDefinitionFromApiResponse(messages);
    }
}
