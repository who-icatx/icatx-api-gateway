package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import edu.stanford.protege.webprotege.entity.OWLEntityData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class AncestorHierarchyNode {

    private List<AncestorHierarchyNode> children = new ArrayList<>();
    private OWLEntityData node;

    @JsonProperty("children")
    public List<AncestorHierarchyNode> getChildren() {
        return children;
    }

    public void setChildren(@JsonProperty("children") List<AncestorHierarchyNode> children) {
        this.children = children;
    }

    @JsonProperty("currentNode")
    public OWLEntityData getNode() {
        return node;
    }

    public void setNode(@JsonProperty("currentNode") OWLEntityData node) {
        this.node = node;
    }
}
