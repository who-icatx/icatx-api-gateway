package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record WhoficEntityPostCoordinationSpecification(@JsonProperty("whoficEntityIri") String whoficEntityIri,
                                                        @JsonProperty("entityType") String entityType,
                                                        @JsonProperty("postcoordinationSpecifications") List<PostCoordinationSpecification> postcoordinationSpecifications) {

    @JsonCreator
    public WhoficEntityPostCoordinationSpecification(@JsonProperty("whoficEntityIri") String whoficEntityIri,
                                                     @JsonProperty("entityType") String entityType,
                                                     @JsonProperty("postcoordinationSpecifications") List<PostCoordinationSpecification> postcoordinationSpecifications) {
        this.whoficEntityIri = whoficEntityIri;
        this.entityType = Objects.requireNonNullElse(entityType, "ICD");
        this.postcoordinationSpecifications = Objects.requireNonNullElseGet(postcoordinationSpecifications, ArrayList::new);
    }
}
