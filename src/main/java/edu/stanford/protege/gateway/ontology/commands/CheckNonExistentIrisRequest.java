package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

@JsonTypeName(CheckNonExistentIrisRequest.CHANNEL)
public record CheckNonExistentIrisRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("iris") Set<IRI> iris
) implements Request<CheckNonExistentIrisResponse> {

    public static final String CHANNEL = "webprotege.entities.CheckNonExistentIris";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
