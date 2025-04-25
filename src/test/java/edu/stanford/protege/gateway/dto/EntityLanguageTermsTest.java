package edu.stanford.protege.gateway.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityLanguageTermsTest {

    @Test
    void testGetFromLanguageTermDto() {
        LanguageTerm title              = new LanguageTerm("Title Label", "titleId");
        LanguageTerm definition         = new LanguageTerm("Definition Label", "defId");
        LanguageTerm longDefinition     = new LanguageTerm("Long Def Label", "longId");
        LanguageTerm fullySpecifiedName = new LanguageTerm("FSN Label", "fsnId");

        List<BaseIndexTerm> baseIndexTerms = List.of(
                new BaseIndexTerm("indexLabel", "indexTypeA", true, "indexTermId")
        );
        List<String> subclassBaseInclusions = List.of("incA", "incB");
        List<BaseExclusionTerm> baseExclusionTerms = List.of(
                new BaseExclusionTerm("exclLabel", "foundationRef", "exclTermId")
        );

        EntityLanguageTermsDto dto = new EntityLanguageTermsDto(
                title,
                definition,
                longDefinition,
                fullySpecifiedName,
                baseIndexTerms,
                subclassBaseInclusions,
                baseExclusionTerms
        );

        EntityLanguageTerms entity1 = EntityLanguageTerms.getFromLanguageTermDto(dto, true);

        assertAll("mapping and obsolete flag",
                () -> assertSame(title,                     entity1.title(),               "title instance"),
                () -> assertSame(definition,                entity1.definition(),          "definition instance"),
                () -> assertSame(longDefinition,            entity1.longDefinition(),      "longDefinition instance"),
                () -> assertSame(fullySpecifiedName,        entity1.fullySpecifiedName(),  "fullySpecifiedName instance"),
                () -> assertSame(baseIndexTerms,            entity1.baseIndexTerms(),      "baseIndexTerms list"),
                () -> assertSame(subclassBaseInclusions,    entity1.subclassBaseInclusions(),"subclassBaseInclusions list"),
                () -> assertSame(baseExclusionTerms,        entity1.baseExclusionTerms(),  "baseExclusionTerms list"),
                () -> assertTrue(entity1.isObsolete(),       "should be marked obsolete")
        );

        EntityLanguageTerms entity2 = EntityLanguageTerms.getFromLanguageTermDto(dto, false);

        assertFalse(entity2.isObsolete(),       "should not be marked obsolete");
    }
}
