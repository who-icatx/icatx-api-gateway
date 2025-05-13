package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.ProjectRequest;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;


@JsonTypeName(GetEntityDirectParentsRequest.CHANNEL)
public record GetEntityDirectParentsRequest(
        @JsonProperty("projectId") @Nonnull ProjectId projectId,
        @JsonProperty("entity") @Nonnull OWLEntity entity
) implements ProjectRequest<GetEntityDirectParentsResponse> {
    public static final String CHANNEL = "webprotege.hierarchies.GetEntityDirectParents";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
