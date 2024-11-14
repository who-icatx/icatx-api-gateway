package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EntityForm(
        @JsonProperty("@id") String id,
        @JsonProperty("label") EntityFormLanguageTerm label,
        @JsonProperty("fullySpecifiedName") EntityFormLanguageTerm fullySpecifiedName,
        @JsonProperty("definition") EntityFormLanguageTerm definition,
        @JsonProperty("longDefinition") EntityFormLanguageTerm longDefinition,
        @JsonProperty("isObsolete") List<String> isObsolete,
        @JsonProperty("baseIndexTerms") List<EntityFormBaseIndexTerm> baseIndexTerms,
        @JsonProperty("subclassBaseInclusions") List<EntityFormSubclassBaseInclusion> subclassBaseInclusions,
        @JsonProperty("baseExclusionTerms") List<EntityFormBaseExclusionTerm> baseExclusionTerms
) {

    public record EntityFormLanguageTerm(
            @JsonProperty("@id") String id,
            @JsonProperty("value") String value
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntityFormBaseIndexTerm(
            @JsonProperty("label") String label,
            @JsonProperty("indexType") EntityFormIndexType indexType,
            @JsonProperty("isInclusion") List<String> isInclusion
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record EntityFormIndexType(
                @JsonProperty("@id") String id,
                @JsonProperty("@type") String type
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntityFormSubclassBaseInclusion(
            @JsonProperty("@id") String id,
            @JsonProperty("@type") String type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntityFormBaseExclusionTerm(
            @JsonProperty("label") String label,
            @JsonProperty("foundationReference") EntityFormFoundationReference foundationReference
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record EntityFormFoundationReference(
                @JsonProperty("@id") String id,
                @JsonProperty("@type") String type
        ) {}
    }
}
