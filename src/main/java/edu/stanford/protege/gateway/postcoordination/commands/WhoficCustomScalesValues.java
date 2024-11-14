package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhoficCustomScalesValues(@JsonProperty("whoficEntityIri") String whoficEntityIri,
                                       @JsonProperty("scaleCustomizations") List<PostCoordinationScaleCustomization> scaleCustomizations) {

    @JsonCreator
    public static WhoficCustomScalesValues create(@JsonProperty("whoficEntityIri") String whoficEntityIri,
                                                  @JsonProperty("scaleCustomizations") List<PostCoordinationScaleCustomization> scaleCustomizations) {
        return new WhoficCustomScalesValues(whoficEntityIri, scaleCustomizations);
    }
}
