package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


public record LinearizationSpecification(LinearizationSpecificationStatus isAuxiliaryAxisChild,
                                         LinearizationSpecificationStatus isGrouping,
                                         LinearizationSpecificationStatus isIncludedInLinearization,
                                         IRI linearizationParent, IRI linearizationView, String codingNote) {

    @JsonCreator
    public LinearizationSpecification(@JsonProperty("isAuxiliaryAxisChild") LinearizationSpecificationStatus isAuxiliaryAxisChild,
                                      @JsonProperty("isGrouping") LinearizationSpecificationStatus isGrouping,
                                      @JsonProperty("isIncludedInLinearization") LinearizationSpecificationStatus isIncludedInLinearization,
                                      @JsonProperty("linearizationParent") IRI linearizationParent,
                                      @JsonProperty("linearizationView") @Nonnull IRI linearizationView,
                                      @JsonProperty("codingNote") String codingNote) {
        this.isAuxiliaryAxisChild = isAuxiliaryAxisChild;
        this.isGrouping = isGrouping;
        this.isIncludedInLinearization = isIncludedInLinearization;
        this.linearizationParent = linearizationParent;
        this.linearizationView = checkNotNull(linearizationView);
        this.codingNote = codingNote;
    }
}
