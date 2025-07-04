package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityLanguageTerms(@JsonProperty("title") LanguageTerm title,
                                  @JsonProperty("definition") LanguageTerm definition,
                                  @JsonProperty("longDefinition") LanguageTerm longDefinition,
                                  @JsonProperty("fullySpecifiedName") LanguageTerm fullySpecifiedName,
                                  @JsonProperty("baseIndexTerms") List<BaseIndexTerm> baseIndexTerms,
                                  @JsonProperty("subclassBaseInclusions") List<String> subclassBaseInclusions,

                                  @JsonProperty("baseExclusionTerms") List<BaseExclusionTerm> baseExclusionTerms,
                                  @JsonProperty("isObsolete") boolean isObsolete,
                                  @JsonProperty("diagnosticCriteria") String diagnosticCriteria

) {

    public static EntityLanguageTerms getFromLanguageTermDto(EntityLanguageTermsDto termsDto, boolean isObsolete, String diagnosticCriteria) {
        return new EntityLanguageTerms(
                termsDto.title(),
                termsDto.definition(),
                termsDto.longDefinition(),
                termsDto.fullySpecifiedName(),
                termsDto.baseIndexTerms(),
                termsDto.subclassBaseInclusions(),
                termsDto.baseExclusionTerms(),
                isObsolete,
                diagnosticCriteria
        );
    }
}
