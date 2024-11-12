package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompositeAxis(String postCoordinationAxis, List<String> subAxis) {

    @JsonCreator
    public CompositeAxis(@JsonProperty("postcoordinationAxis") String postCoordinationAxis, @JsonProperty("replacedBySubaxes") List<String> subAxis) {
        this.postCoordinationAxis = postCoordinationAxis;
        this.subAxis = subAxis;
    }
}
