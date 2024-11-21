package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.gateway.ontology.commands.GetIsExistingProjectRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetIsExistingProjectRequest(
        @JsonProperty("projectId") ProjectId projectId
) implements Request<GetIsExistingProjectResponse> {
    public static final String CHANNEL = "webprotege.projects.GetIsExistingProject";

    public static GetIsExistingProjectRequest create(ProjectId projectId) {
        return new GetIsExistingProjectRequest(projectId);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
