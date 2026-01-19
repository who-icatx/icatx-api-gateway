package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

import static edu.stanford.protege.gateway.ontology.commands.CheckNonExistentIrisRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CheckNonExistentIrisResponse(
        @JsonProperty("nonExistentIris") Set<IRI> nonExistentIris
) implements Response {

    public static CheckNonExistentIrisResponse create(Set<IRI> nonExistentIris) {
        return new CheckNonExistentIrisResponse(nonExistentIris);
    }
}
