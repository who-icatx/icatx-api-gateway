package edu.stanford.protege.gateway.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityLanguageTermsDtoTest {

    @Test
    void testGetFromTerms() {
        LanguageTerm title              = new LanguageTerm("Title Label", "titleId");
        LanguageTerm definition         = new LanguageTerm("Definition Label", "defId");
        LanguageTerm longDefinition     = new LanguageTerm("Long Def Label", "longId");
        LanguageTerm fullySpecifiedName = new LanguageTerm("FSN Label", "fsnId");

        List<BaseIndexTerm> baseIndexTerms = List.of(
                new BaseIndexTerm("indexTermLabel",
                        "someIndexType",
                        true,
                        false,
                        "indexTermId")
        );
        List<String> subclassBaseInclusions = List.of("inclusionA", "inclusionB");
        List<BaseExclusionTerm> baseExclusionTerms = List.of(
                new BaseExclusionTerm("exclusionLabel",
                        "foundationReference",
                        "exclusionTermId")
        );

        EntityLanguageTerms entityLanguageTerms = new EntityLanguageTerms(
                title,
                definition,
                longDefinition,
                fullySpecifiedName,
                baseIndexTerms,
                subclassBaseInclusions,
                baseExclusionTerms,
                false,
                null
        );

        EntityLanguageTermsDto dto = EntityLanguageTermsDto.getFromTerms(entityLanguageTerms);

        assertAll("getFromTerms mapping",
                () -> assertSame(title,               dto.title(),                "title instance"),
                () -> assertSame(definition,          dto.definition(),           "definition instance"),
                () -> assertSame(longDefinition,      dto.longDefinition(),       "longDefinition instance"),
                () -> assertSame(fullySpecifiedName,  dto.fullySpecifiedName(),   "fullySpecifiedName instance"),
                () -> assertSame(baseIndexTerms,      dto.baseIndexTerms(),       "baseIndexTerms list"),
                () -> assertSame(subclassBaseInclusions,
                        dto.subclassBaseInclusions(),
                        "subclassBaseInclusions list"),
                () -> assertSame(baseExclusionTerms,
                        dto.baseExclusionTerms(),
                        "baseExclusionTerms list")
        );
    }
}
