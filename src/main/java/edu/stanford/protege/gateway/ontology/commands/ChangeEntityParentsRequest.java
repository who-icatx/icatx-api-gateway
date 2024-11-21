package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;

@JsonTypeName(ChangeEntityParentsRequest.CHANNEL)
public record ChangeEntityParentsRequest(@JsonProperty("changeRequestId") ChangeRequestId changeRequestId,
                                         @JsonProperty("projectId") @Nonnull ProjectId projectId,
                                         @JsonProperty("parents") @Nonnull ImmutableSet<OWLClass> parents,
                                         @JsonProperty("entity") @Nonnull OWLClass entity,
                                         @JsonProperty("commitMessage") @Nonnull String commitMessage) implements Request<ChangeEntityParentsResponse> {
    public static final String CHANNEL = "webprotege.entities.ChangeEntityParents";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
