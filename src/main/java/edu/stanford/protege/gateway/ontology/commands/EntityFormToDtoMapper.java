package edu.stanford.protege.gateway.ontology.commands;

import edu.stanford.protege.gateway.dto.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.stream.Collectors;

public class EntityFormToDtoMapper {

    public static EntityLanguageTerms mapFormToTerms(EntityForm entityForm) {
        LanguageTerm label = null;
        LanguageTerm fullySpecifiedName = null;
        LanguageTerm definition = null;
        LanguageTerm longDefinition = null;
        List<String> icfReferencesIris = new ArrayList<>();
        List<BaseIndexTerm> baseIndexTerms = new ArrayList<>();
        List<String> subclassBaseInclusions = new ArrayList<>();
        List<BaseExclusionTerm> baseExclusionTerms = new ArrayList<>();
        boolean isObsolete = false;
        String diagnosticCriteria = null;

        if (entityForm != null) {
            if (entityForm.label() != null) {
                label = new LanguageTerm(entityForm.label().value(), entityForm.label().id());
            }
            if (entityForm.fullySpecifiedName() != null) {
                fullySpecifiedName = new LanguageTerm(entityForm.fullySpecifiedName().value(), entityForm.fullySpecifiedName().id());
            }
            if (entityForm.definition() != null) {
                definition = new LanguageTerm(entityForm.definition().value(), entityForm.definition().id());
            }
            if (entityForm.longDefinition() != null) {
                longDefinition = new LanguageTerm(entityForm.longDefinition().value(), entityForm.longDefinition().id());
            }

            if (entityForm.baseIndexTerms() != null) {
                baseIndexTerms = mapBaseIndexTerms(entityForm.baseIndexTerms());
            }
            if (entityForm.subclassBaseInclusions() != null) {
                subclassBaseInclusions = extractSubclassBaseInclusions(entityForm.subclassBaseInclusions());
            }
            if (entityForm.baseExclusionTerms() != null) {
                baseExclusionTerms = mapBaseExclusionTerms(entityForm.baseExclusionTerms());
            }
            isObsolete = getBooleanOutOfStringArray(entityForm.isObsolete());
            diagnosticCriteria = entityForm.diagnosticCriteria();


            if(entityForm.icfReferences() != null) {
                icfReferencesIris = entityForm.icfReferences().stream().map(entity -> entity.getIRI().toString()).toList();
            }
        }



        return new EntityLanguageTerms(label,
                definition,
                longDefinition,
                fullySpecifiedName,
                baseIndexTerms,
                subclassBaseInclusions,
                baseExclusionTerms,
                isObsolete,
                diagnosticCriteria,
                icfReferencesIris);
    }

    public static EntityForm mapFromDto(String entityIri, EntityLanguageTerms languageTerms) {

        List<EntityForm.EntityFormBaseIndexTerm> baseIndexTerms = languageTerms.baseIndexTerms()
                .stream()
                .map(EntityFormToDtoMapper::mapFromDto)
                .toList();

        List<EntityForm.EntityFormSubclassBaseInclusion> subclassBaseInclusions = languageTerms.subclassBaseInclusions()
                .stream()
                .map(EntityFormToDtoMapper::mapFromDto)
                .toList();

        List<EntityForm.EntityFormBaseExclusionTerm> baseExclusionTerms = languageTerms.baseExclusionTerms()
                .stream()
                .map(EntityFormToDtoMapper::mapFromDto)
                .toList();

        List<OWLEntity> icfRelatedEntities = languageTerms.relatedIcfEntities().stream().map(iri -> new OWLClassImpl(IRI.create(iri))).collect(Collectors.toList());

        EntityForm.EntityFormLanguageTerm label = new EntityForm.EntityFormLanguageTerm(languageTerms.title().termId(), languageTerms.title().label());
        EntityForm.EntityFormLanguageTerm fullySpecifiedName = new EntityForm.EntityFormLanguageTerm(languageTerms.fullySpecifiedName().termId(), languageTerms.fullySpecifiedName().label());
        EntityForm.EntityFormLanguageTerm definition = new EntityForm.EntityFormLanguageTerm(languageTerms.definition().termId(), languageTerms.definition().label());
        EntityForm.EntityFormLanguageTerm longDefinition = new EntityForm.EntityFormLanguageTerm(languageTerms.longDefinition().termId(), languageTerms.longDefinition().label());


        return new EntityForm(entityIri,
                label,
                fullySpecifiedName,
                definition,
                longDefinition,
                Collections.singletonList(String.valueOf(languageTerms.isObsolete())),
                baseIndexTerms,
                subclassBaseInclusions,
                baseExclusionTerms,
                languageTerms.diagnosticCriteria(),
                icfRelatedEntities
        );
    }

    private static List<BaseIndexTerm> mapBaseIndexTerms(List<EntityForm.EntityFormBaseIndexTerm> formBaseIndexTerms) {
        return formBaseIndexTerms.stream().map(formBaseIndexTerm -> {
                            String indexType = "";
                            if (formBaseIndexTerm.indexType() != null) {
                                indexType = formBaseIndexTerm.indexType().id();
                            }

                            return new BaseIndexTerm(
                                    formBaseIndexTerm.label(),
                                    indexType,
                                    getBooleanOutOfStringArray(formBaseIndexTerm.isInclusion()),
                                    getBooleanOutOfStringArray(formBaseIndexTerm.isDeprecated()),
                                    formBaseIndexTerm.id()
                            );
                        }
                )
                .sorted(Comparator.comparing(BaseIndexTerm::label))
                .collect(Collectors.toList());
    }

    private static List<String> extractSubclassBaseInclusions(List<EntityForm.EntityFormSubclassBaseInclusion> subclassBaseInclusions) {
        return subclassBaseInclusions.stream().map(EntityForm.EntityFormSubclassBaseInclusion::id).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    }

    private static List<BaseExclusionTerm> mapBaseExclusionTerms(List<EntityForm.EntityFormBaseExclusionTerm> baseExclusionTerms) {
        return baseExclusionTerms.stream().map((EntityForm.EntityFormBaseExclusionTerm baseExclusionTerm) -> {
                    String id = "";
                    if (baseExclusionTerm.foundationReference() != null) {
                        id = baseExclusionTerm.foundationReference().id();
                    }
                    return new BaseExclusionTerm(baseExclusionTerm.label(), id, baseExclusionTerm.id());
                })
                .sorted((p, q) -> {
                    if (p.termId() != null && q.termId() != null) {
                        return p.termId().compareTo(q.termId());
                    }
                    if (p.label() != null && q.label() != null) {
                        return p.label().compareTo(q.label());
                    }
                    if (p.foundationReference() != null && q.foundationReference() != null) {
                        return p.foundationReference().compareTo(q.foundationReference());
                    }
                    return -1;
                })
                .collect(Collectors.toList());
    }

    private static boolean getBooleanOutOfStringArray(List<String> stringArray) {
        if (stringArray != null && !stringArray.isEmpty()) {
            return Boolean.parseBoolean(stringArray.get(0));
        }
        return false;
    }

    private static EntityForm.EntityFormBaseIndexTerm mapFromDto(BaseIndexTerm baseIndexTerm) {
        return new EntityForm.EntityFormBaseIndexTerm(
                baseIndexTerm.label(),
                new EntityForm.EntityFormBaseIndexTerm.EntityFormIndexType(baseIndexTerm.indexType(), "NamedIndividual"),
                Collections.singletonList(String.valueOf(baseIndexTerm.isInclusion())),
                Collections.singletonList(String.valueOf(baseIndexTerm.isDeprecated())),
                baseIndexTerm.termId()
        );
    }


    private static EntityForm.EntityFormSubclassBaseInclusion mapFromDto(String subclassBaseInclusion) {
        return new EntityForm.EntityFormSubclassBaseInclusion(subclassBaseInclusion, "Class");
    }

    private static EntityForm.EntityFormBaseExclusionTerm mapFromDto(BaseExclusionTerm baseExclusionTerm) {
        return new EntityForm.EntityFormBaseExclusionTerm(
                baseExclusionTerm.termId(),
                baseExclusionTerm.label(),
                new EntityForm.EntityFormBaseExclusionTerm.EntityFormFoundationReference(baseExclusionTerm.foundationReference(), "Class"));
    }
}
