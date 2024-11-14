package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LinearizationResiduals {

    private final String suppressOtherSpecifiedResiduals;

    private final String suppressUnspecifiedResiduals;


    private final String unspecifiedResidualTitle;
    private final String otherSpecifiedResidualTitle;


    @JsonCreator
    public LinearizationResiduals( @JsonProperty("suppressOtherSpecifiedResiduals") String suppressOtherSpecifiedResiduals,
                                   @JsonProperty("suppressUnspecifiedResiduals") String suppressUnspecifiedResiduals,
                                   @JsonProperty("otherSpecifiedResidualTitle") String otherSpecifiedResidualTitle,
                                   @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle) {
        this.suppressOtherSpecifiedResiduals = suppressOtherSpecifiedResiduals;
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
        this.suppressUnspecifiedResiduals = suppressUnspecifiedResiduals;
        this.otherSpecifiedResidualTitle = otherSpecifiedResidualTitle;
    }


    @JsonProperty("suppressOtherSpecifiedResiduals")
    public String getSuppressOtherSpecifiedResiduals() {
        return suppressOtherSpecifiedResiduals;
    }

    @JsonProperty("unspecifiedResidualTitle")
    public String getUnspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }

    @JsonProperty("suppressUnspecifiedResiduals")
    public String getSuppressUnspecifiedResiduals() {
        return suppressUnspecifiedResiduals;
    }

    @JsonProperty("otherSpecifiedResidualTitle")
    public String getOtherSpecifiedResidualTitle() {
        return otherSpecifiedResidualTitle;
    }
}
