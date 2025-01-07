package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

import static edu.stanford.protege.gateway.ontology.commands.FilterExistingEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record FilterExistingEntitiesResponse(
        @JsonProperty("existingEntities") Set<IRI> existingEntities) implements Response {
    public static FilterExistingEntitiesResponse create(Set<IRI> existingEntities) {
        return new FilterExistingEntitiesResponse(existingEntities);
    }
}
