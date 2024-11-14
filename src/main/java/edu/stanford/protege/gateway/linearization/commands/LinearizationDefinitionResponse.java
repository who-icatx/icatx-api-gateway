package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.gateway.linearization.commands.LinearizationDefinitionRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record LinearizationDefinitionResponse(
        @JsonProperty("definitionList") List<LinearizationDefinition> definitionList) implements Response {

}
