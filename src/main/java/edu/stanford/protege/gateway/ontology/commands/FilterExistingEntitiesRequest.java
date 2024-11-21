package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

import static edu.stanford.protege.gateway.ontology.commands.FilterExistingEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record FilterExistingEntitiesRequest(@JsonProperty("projectId") ProjectId projectId,
                                            @JsonProperty("iris") Set<IRI> iris) implements Request<FilterExistingEntitiesResponse> {

    public static final String CHANNEL = "webprotege.entities.FilterExistingEntities";

    public static FilterExistingEntitiesRequest create(ProjectId projectId, Set<IRI> iriSet) {
        return new FilterExistingEntitiesRequest(projectId, iriSet);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
