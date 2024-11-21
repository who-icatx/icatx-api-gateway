package edu.stanford.protege.gateway.ontology.commands;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.entity.OWLEntityData;

import javax.annotation.Nonnull;
import java.util.Set;

@JsonTypeName(ChangeEntityParentsRequest.CHANNEL)
public record ChangeEntityParentsResponse(@JsonProperty("classesWithCycle") @Nonnull Set<OWLEntityData> classesWithCycle,
                                          @JsonProperty("classesWithRetiredParents") @Nonnull Set<OWLEntityData> classesWithRetiredParents) implements Response {
}
