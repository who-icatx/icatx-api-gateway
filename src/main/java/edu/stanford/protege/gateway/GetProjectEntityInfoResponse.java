package edu.stanford.protege.gateway;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.GetProjectEntityInfoRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetProjectEntityInfoResponse(OWLEntityDto entityDto) implements Response {
}
