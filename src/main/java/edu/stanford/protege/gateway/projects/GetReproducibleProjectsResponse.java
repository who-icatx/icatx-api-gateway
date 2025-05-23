package edu.stanford.protege.gateway.projects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.dispatch.Result;

import java.util.List;


@JsonTypeName(GetReproducibleProjectsRequest.CHANNEL)
public record GetReproducibleProjectsResponse(List<ReproducibleProject> reproducibleProjectList) implements Result {
}
