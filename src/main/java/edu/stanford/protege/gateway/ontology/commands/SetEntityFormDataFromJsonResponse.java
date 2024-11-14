package edu.stanford.protege.gateway.ontology.commands;


import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

@JsonTypeName(SetEntityFormDataFromJsonRequest.CHANNEL)
public record SetEntityFormDataFromJsonResponse() implements Response {
}
