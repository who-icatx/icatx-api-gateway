package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;


@JsonTypeName(GetTablePostCoordinationAxisRequest.CHANNEL)
public record GetTablePostCoordinationAxisResponse(TableConfiguration tableConfiguration) implements Response {

}
