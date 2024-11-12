package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LinearizationResiduals(LinearizationSpecificationStatus suppressOtherSpecifiedResiduals,
                                     LinearizationSpecificationStatus suppressUnspecifiedResiduals,
                                     String otherSpecifiedResidualTitle,
                                     String unspecifiedResidualTitle) {

    @JsonCreator
    public LinearizationResiduals(@JsonProperty("suppressOtherSpecifiedResiduals") LinearizationSpecificationStatus suppressOtherSpecifiedResiduals,
                                  @JsonProperty("suppressUnspecifiedResiduals") LinearizationSpecificationStatus suppressUnspecifiedResiduals,
                                  @JsonProperty("otherSpecifiedResidualTitle") String otherSpecifiedResidualTitle,
                                  @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle) {
        this.suppressUnspecifiedResiduals = suppressUnspecifiedResiduals;
        this.suppressOtherSpecifiedResiduals = suppressUnspecifiedResiduals;
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
        this.otherSpecifiedResidualTitle = otherSpecifiedResidualTitle;
    }


    @Override
    @JsonProperty("suppressOtherSpecifiedResiduals")
    public LinearizationSpecificationStatus suppressOtherSpecifiedResiduals() {
        return suppressOtherSpecifiedResiduals;
    }

    @Override
    @JsonProperty("unspecifiedResidualTitle")
    public String unspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }

    @Override
    @JsonProperty("suppressUnspecifiedResiduals")
    public LinearizationSpecificationStatus suppressUnspecifiedResiduals() {
        return suppressUnspecifiedResiduals;
    }

    @Override
    @JsonProperty("otherSpecifiedResidualTitle")
    public String otherSpecifiedResidualTitle() {
        return otherSpecifiedResidualTitle;
    }
}
