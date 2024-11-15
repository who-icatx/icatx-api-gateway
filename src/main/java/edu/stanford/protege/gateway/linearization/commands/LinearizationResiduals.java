package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinearizationResiduals(@JsonProperty("suppressOtherSpecifiedResiduals") LinearizationSpecificationStatus suppressOtherSpecifiedResiduals,
                                     @JsonProperty("suppressUnspecifiedResiduals") LinearizationSpecificationStatus suppressUnspecifiedResiduals,
                                     @JsonProperty("otherSpecifiedResidualTitle") String otherSpecifiedResidualTitle,
                                     @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle) {

}
