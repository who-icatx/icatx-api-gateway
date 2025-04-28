package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityLanguageTermsDto(@JsonProperty("title") LanguageTerm title,
                                     @JsonProperty("definition") LanguageTerm definition,
                                     @JsonProperty("longDefinition") LanguageTerm longDefinition,
                                     @JsonProperty("fullySpecifiedName") LanguageTerm fullySpecifiedName,
                                     @JsonProperty("baseIndexTerms") List<BaseIndexTerm> baseIndexTerms,
                                     @JsonProperty("subclassBaseInclusions") List<String> subclassBaseInclusions,
                                     @JsonProperty("baseExclusionTerms") List<BaseExclusionTerm> baseExclusionTerms) {
    public static EntityLanguageTermsDto getFromTerms(EntityLanguageTerms entityLanguageTerms) {
        return new EntityLanguageTermsDto(
                entityLanguageTerms.title(),
                entityLanguageTerms.definition(),
                entityLanguageTerms.longDefinition(),
                entityLanguageTerms.fullySpecifiedName(),
                entityLanguageTerms.baseIndexTerms(),
                entityLanguageTerms.subclassBaseInclusions(),
                entityLanguageTerms.baseExclusionTerms()
        );
    }
}
