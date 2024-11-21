package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;

import static edu.stanford.protege.gateway.ontology.commands.GetAvailableProjectsForApiRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetAvailableProjectsForApiRequest() implements Request<GetAvailableProjectsForApiResponse> {
    public static final String CHANNEL = "webprotege.projects.GetAvailableProjectsForApi";

    public static GetAvailableProjectsForApiRequest create() {
        return new GetAvailableProjectsForApiRequest();
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
