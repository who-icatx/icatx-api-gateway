package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TableConfiguration(String entityType,
                                 List<String> postCoordinationAxes,
                                 List<CompositeAxis> compositePostCoordinationAxes) {


    public final static String DEFINITIONS_COLLECTION = "PostCoordinationTableConfiguration";

    @JsonCreator
    public TableConfiguration(@JsonProperty("entityType") String entityType, @JsonProperty("postcoordinationAxes") List<String> postCoordinationAxes, @JsonProperty("compositePostcoordinationAxes") List<CompositeAxis> compositePostCoordinationAxes) {
        this.entityType = entityType;
        this.postCoordinationAxes = postCoordinationAxes;
        this.compositePostCoordinationAxes = compositePostCoordinationAxes;
    }
}
