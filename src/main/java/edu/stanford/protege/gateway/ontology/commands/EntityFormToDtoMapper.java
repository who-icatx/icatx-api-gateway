package edu.stanford.protege.gateway.ontology.commands;

import edu.stanford.protege.gateway.dto.BaseExclusionTerm;
import edu.stanford.protege.gateway.dto.BaseIndexTerm;
import edu.stanford.protege.gateway.dto.EntityLanguageTerms;
import edu.stanford.protege.gateway.dto.LanguageTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityFormToDtoMapper {


    public static EntityLanguageTerms mapFormToTerms(EntityForm entityForm) {
        LanguageTerm label = null;
        LanguageTerm fullySpecifiedName = null;
        LanguageTerm definition = null;
        LanguageTerm longDefinition = null;

        List<BaseIndexTerm> baseIndexTerms = new ArrayList<>();
        List<String> subclassBaseInclusions = new ArrayList<>();
        List<BaseExclusionTerm> baseExclusionTerms = new ArrayList<>();
        boolean isObsolete = false;

        if(entityForm != null) {
            if(entityForm.label() != null) {
                label = new LanguageTerm(entityForm.label().value(), entityForm.label().id());
            }
            if(entityForm.fullySpecifiedName() != null) {
                fullySpecifiedName = new LanguageTerm(entityForm.fullySpecifiedName().value(), entityForm.fullySpecifiedName().id());
            }
            if(entityForm.definition() != null) {
                definition = new LanguageTerm(entityForm.definition().value(), entityForm.definition().id());
            }
            if(entityForm.longDefinition() != null) {
                longDefinition = new LanguageTerm(entityForm.longDefinition().value(), entityForm.longDefinition().id());
            }

            if(entityForm.baseIndexTerms() != null) {
                baseIndexTerms = mapBaseIndexTerms(entityForm.baseIndexTerms());
            }
            if(entityForm.subclassBaseInclusions() != null) {
                subclassBaseInclusions = extractSubclassBaseInclusions(entityForm.subclassBaseInclusions());
            }
            if(entityForm.baseExclusionTerms() != null) {
                baseExclusionTerms = mapBaseExclusionTerms(entityForm.baseExclusionTerms());
            }
            isObsolete = getBooleanOutOfStringArray(entityForm.isObsolete());

        }



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
