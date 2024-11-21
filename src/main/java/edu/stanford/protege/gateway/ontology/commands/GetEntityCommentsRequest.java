package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityCommentsRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityCommentsRequest(@JsonProperty("projectId") ProjectId projectId,
                                       @JsonProperty("entityIri") String entityIri) implements Request<GetEntityCommentsResponse> {
    public static final String CHANNEL = "icatx.webprotege.discussions.GetEntityComments";

    public static GetEntityCommentsRequest create(ProjectId projectId,
                                                  String entityIri) {
        return new GetEntityCommentsRequest(projectId, entityIri);
    }


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}