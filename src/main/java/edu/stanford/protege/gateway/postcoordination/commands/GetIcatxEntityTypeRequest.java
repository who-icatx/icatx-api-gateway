package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;


@JsonTypeName(GetIcatxEntityTypeRequest.CHANNEL)
public record GetIcatxEntityTypeRequest(@JsonProperty("entityIri") IRI entityIri,
                                        @JsonProperty("projectId") ProjectId projectId) implements Request<GetIcatxEntityTypeResponse> {

    public static final String CHANNEL = "webprotege.entities.GetIcatxEntityType";

    public static GetIcatxEntityTypeRequest create(IRI entityIri,
                                                   ProjectId projectId) {
        return new GetIcatxEntityTypeRequest(entityIri, projectId);
    }


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
