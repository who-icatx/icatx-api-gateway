package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityFormAsJsonRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityFormAsJsonRequest(@JsonProperty("projectId") ProjectId projectId,
                                         @JsonProperty("entityIri") String entityIri,
                                         @JsonProperty("formId") String formId) implements Request<GetEntityFormAsJsonResponse> {

    public static final String CHANNEL = "webprotege.forms.GetEntityFormAsJson";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
