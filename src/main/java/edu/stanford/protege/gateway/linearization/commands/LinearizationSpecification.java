package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


public class LinearizationSpecification  {

    private final String isAuxiliaryAxisChild;

    private final String isGrouping;

    private final String isIncludedInLinearization;

    private final IRI linearizationParent;

    private final IRI linearizationView;

    private final String codingNote;

    @JsonCreator
    public LinearizationSpecification(@JsonProperty("isAuxiliaryAxisChild") String isAuxiliaryAxisChild,
                                      @JsonProperty("isGrouping") String isGrouping,
                                      @JsonProperty("isIncludedInLinearization") String isIncludedInLinearization,
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


    public String getIsAuxiliaryAxisChild() {
        return isAuxiliaryAxisChild;
    }

    public String getIsGrouping() {
        return isGrouping;
    }

    public String getIsIncludedInLinearization() {
        return isIncludedInLinearization;
    }

    public IRI getLinearizationParent() {
        return linearizationParent;
    }

    public IRI getLinearizationView() {
        return linearizationView;
    }

    public String getCodingNote() {
        return codingNote;
    }
}
