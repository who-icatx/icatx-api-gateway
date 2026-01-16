package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;


@JsonTypeName(GetFullPostCoordinationConfigurationRequest.CHANNEL)
public record GetFullPostCoordinationConfigurationResponse(List<CompletePostCoordinationAxisConfiguration> configuration) implements Response {
}
