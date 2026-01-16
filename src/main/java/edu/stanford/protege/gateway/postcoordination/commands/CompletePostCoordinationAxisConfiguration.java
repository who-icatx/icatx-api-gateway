package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

public class CompletePostCoordinationAxisConfiguration {
    @JsonProperty("axisIdentifier")
    public String axisIdentifier;
    @JsonProperty("availableForEntityTypes")
    public Set<String> availableForEntityTypes;
    @JsonProperty("tableLabel")
    public String tableLabel;
    @JsonProperty("scaleLabel")
    public String scaleLabel;
    @JsonProperty("postCoordinationAxisSortingCode")
    public String postCoordinationAxisSortingCode;
    @JsonProperty("postCoordinationSubAxes")
    public Map<String, Set<String>> postCoordinationSubAxes;
    @JsonProperty("availableScalesTopClass")
    public String availableScalesTopClass;
    @JsonProperty("allowMultiValue")
    public String allowMultivalue;
}
