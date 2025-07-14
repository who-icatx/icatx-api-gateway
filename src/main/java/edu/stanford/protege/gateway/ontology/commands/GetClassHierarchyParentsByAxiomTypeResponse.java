package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.entity.OWLEntityData;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static edu.stanford.protege.webprotege.hierarchy.GetClassHierarchyParentsByAxiomTypeAction.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetClassHierarchyParentsByAxiomTypeResponse(@JsonProperty("owlClass") @Nullable OWLClass owlClass,
                                                          @JsonProperty("parentsBySubclassOf") @Nonnull Set<OWLEntityData> parentsBySubclassOf,
                                                          @JsonProperty("parentsByEquivalentClass") @Nonnull Set<OWLEntityData> parentsByEquivalentClass)
        implements Response {
}
