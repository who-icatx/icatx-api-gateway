package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

import static edu.stanford.protege.gateway.ontology.commands.FilterExistingEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record FilterExistingEntitiesResponse(@JsonProperty("existingEntities") Set<IRI> existingEntities) implements Response {
}
