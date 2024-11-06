package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.ontology.commands.GetClassAncestorsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public class GetClassAncestorsResponse implements Response {
    private final AncestorHierarchyNode ancestorClassHierarchy;


    @JsonCreator
    public GetClassAncestorsResponse(@JsonProperty("ancestorTree") AncestorHierarchyNode ancestorClassHierarchy) {
        this.ancestorClassHierarchy = ancestorClassHierarchy;
    }

    public AncestorHierarchyNode getAncestorClassHierarchy() {
        return ancestorClassHierarchy;
    }
}
