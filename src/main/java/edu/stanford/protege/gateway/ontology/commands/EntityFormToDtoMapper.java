package edu.stanford.protege.gateway.ontology.commands;

import edu.stanford.protege.gateway.dto.BaseExclusionTerm;
import edu.stanford.protege.gateway.dto.BaseIndexTerm;
import edu.stanford.protege.gateway.dto.EntityLanguageTerms;
import edu.stanford.protege.gateway.dto.LanguageTerm;

import java.util.List;
import java.util.stream.Collectors;

public class EntityFormToDtoMapper {


    public static EntityLanguageTerms mapFormToTerms(EntityForm entityForm) {
        LanguageTerm label = new LanguageTerm(entityForm.label().value(), entityForm.label().id());
        LanguageTerm fullySpecifiedName = new LanguageTerm(entityForm.fullySpecifiedName().value(), entityForm.fullySpecifiedName().id());
        LanguageTerm definition = new LanguageTerm(entityForm.definition().value(), entityForm.definition().id());
        LanguageTerm longDefinition = new LanguageTerm(entityForm.longDefinition().value(), entityForm.longDefinition().id());
        List<BaseIndexTerm> baseIndexTerms = mapBaseIndexTerms(entityForm.baseIndexTerms());
        List<String> subclassBaseInclusions = extractSubclassBaseInclusions(entityForm.subclassBaseInclusions());
        List<BaseExclusionTerm> baseExclusionTerms = mapBaseExclusionTerms(entityForm.baseExclusionTerms());
        boolean isObsolete = getBooleanOutOfStringArray(entityForm.isObsolete());

        return new EntityLanguageTerms(label,
                definition,
                longDefinition,
                fullySpecifiedName,
                baseIndexTerms,
                subclassBaseInclusions,
                baseExclusionTerms,
                isObsolete);
    }


    private static List<BaseIndexTerm> mapBaseIndexTerms(List<EntityForm.EntityFormBaseIndexTerm> formBaseIndexTerms) {
        return formBaseIndexTerms.stream().map(formBaseIndexTerm -> {
            String indexType = formBaseIndexTerm.indexType().id().toLowerCase().contains("synonym") ? "Synonym" : "Narrower";
            return new BaseIndexTerm(formBaseIndexTerm.label(), indexType, getBooleanOutOfStringArray(formBaseIndexTerm.isInclusion()), formBaseIndexTerm.indexType().id());
        }).collect(Collectors.toList());
    }

    private static List<String> extractSubclassBaseInclusions(List<EntityForm.EntityFormSubclassBaseInclusion> subclassBaseInclusions) {
        return subclassBaseInclusions.stream().map(EntityForm.EntityFormSubclassBaseInclusion::id).collect(Collectors.toList());
    }

    private static List<BaseExclusionTerm> mapBaseExclusionTerms(List<EntityForm.EntityFormBaseExclusionTerm> baseExclusionTerms) {
        return baseExclusionTerms.stream().map(baseExclusionTerm ->
                        new BaseExclusionTerm(baseExclusionTerm.label(), baseExclusionTerm.foundationReference().id(), ""))
                .collect(Collectors.toList());
    }

    private static boolean getBooleanOutOfStringArray(List<String> stringArray) {
        if (stringArray != null && !stringArray.isEmpty()) {
            return Boolean.parseBoolean(stringArray.get(0));
        }
        return false;
    }


}
