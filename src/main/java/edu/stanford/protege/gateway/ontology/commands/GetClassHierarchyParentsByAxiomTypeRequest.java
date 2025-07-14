package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import edu.stanford.protege.webprotege.hierarchy.ClassHierarchyDescriptor;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


@JsonTypeName(GetClassHierarchyParentsByAxiomTypeRequest.CHANNEL)
public record GetClassHierarchyParentsByAxiomTypeRequest(@JsonProperty("projectId") @Nonnull ProjectId projectId,
                                                         @JsonProperty("owlClass") @Nonnull OWLClass owlClass,
                                                         @JsonProperty("classHierarchyDescriptor") @Nonnull ClassHierarchyDescriptor classHierarchyDescriptor)
        implements Request<GetClassHierarchyParentsByAxiomTypeResponse> {

    public static final String CHANNEL = "webprotege.hierarchies.GetClassHierarchyParentsByAxiomType";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public GetClassHierarchyParentsByAxiomTypeRequest(@JsonProperty("projectId") @Nonnull ProjectId projectId,
                                                      @JsonProperty("owlClass") @Nonnull OWLClass owlClass,
                                                      @JsonProperty("classHierarchyDescriptor") @Nonnull ClassHierarchyDescriptor classHierarchyDescriptor) {
        this.projectId = checkNotNull(projectId);
        this.owlClass = checkNotNull(owlClass);
        this.classHierarchyDescriptor = checkNotNull(classHierarchyDescriptor);
    }
}
