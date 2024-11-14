package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.OWLClass;

@JsonTypeName(GetLogicalDefinitionsRequest.CHANNEL)
public record GetLogicalDefinitionsRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("subject") OWLClass subject
        ) implements Request<GetLogicalDefinitionsResponse> {


    public final static String CHANNEL = "icatx.logicalDefinitions.GetLogicalDefinitions";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
