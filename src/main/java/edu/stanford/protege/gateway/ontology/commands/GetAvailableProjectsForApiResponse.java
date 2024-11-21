package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.gateway.dto.ProjectSummaryDto;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.gateway.ontology.commands.GetAvailableProjectsForApiRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetAvailableProjectsForApiResponse(
        @JsonProperty("availableProjects") List<ProjectSummaryDto> availableProjects
) implements Response {
}
