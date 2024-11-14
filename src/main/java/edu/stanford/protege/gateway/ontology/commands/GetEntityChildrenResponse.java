package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityChildrenRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityChildrenResponse(@JsonProperty("childrenIris") List<IRI> childrenIris) implements Response {
}
