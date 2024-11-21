package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.webprotege.common.*;

import javax.annotation.Nullable;

import static edu.stanford.protege.gateway.ontology.commands.CreateClassesFromApiRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record CreateClassesFromApiRequest(@JsonProperty("changeRequestId") ChangeRequestId changeRequestId,
                                          @JsonProperty("projectId") ProjectId projectId,
                                          @JsonProperty("sourceText") String sourceText,
                                          @JsonProperty("langTag") @Nullable String langTag,
                                          @JsonProperty("parent") ImmutableSet<String> parent) implements Request<CreateClassesFromApiResponse> {
    public static final String CHANNEL = "icatx.webprotege.entities.CreateClassesFromApi";

    public static CreateClassesFromApiRequest create(ChangeRequestId changeRequestId,
                                                     ProjectId projectId,
                                                     String sourceText,
                                                     @Nullable String langTag,
                                                     ImmutableSet<String> parents) {
        return new CreateClassesFromApiRequest(changeRequestId, projectId, sourceText, langTag, parents);
    }

    public String getChannel() {
        return CHANNEL;
    }
}