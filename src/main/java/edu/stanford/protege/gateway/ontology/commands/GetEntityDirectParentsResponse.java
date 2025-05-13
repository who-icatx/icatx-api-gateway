package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.entity.OWLEntityData;

import javax.annotation.Nonnull;
import java.util.List;

@JsonTypeName(GetEntityDirectParentsRequest.CHANNEL)
public record GetEntityDirectParentsResponse(
        @JsonProperty("entity") @Nonnull OWLEntityData entity,
        @JsonProperty("directParents") List<EntityNode> directParents
) implements Response {

}
