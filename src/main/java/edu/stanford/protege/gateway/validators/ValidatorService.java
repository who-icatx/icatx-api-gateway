package edu.stanford.protege.gateway.validators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.gateway.EntityIsMissingException;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.ValidationException;
import edu.stanford.protege.gateway.dto.BaseExclusionTerm;
import edu.stanford.protege.gateway.dto.CreateEntityDto;
import edu.stanford.protege.gateway.dto.EntityLanguageTermsDto;
import edu.stanford.protege.gateway.dto.LogicalConditions;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.postcoordination.CustomScalesMapper;
import edu.stanford.protege.gateway.postcoordination.SpecificationMapper;
import edu.stanford.protege.gateway.postcoordination.commands.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.criteria.EntityTypeIsOneOfCriteria;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.search.DeprecatedEntitiesTreatment;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class ValidatorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);

    private final CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;
    private final CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;

    private final CommandExecutor<GetExistingClassesForApiRequest, GetExistingClassesForApiResponse> getEntitySearchExecutor;
    private final CommandExecutor<ValidateEntityUpdateRequest, ValidateEntityUpdateResponse> validateEntityUpdateExecutor;
    private final CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor;
    private final CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> entityTypesExecutor;
    private final CommandExecutor<CheckNonExistentIrisRequest, CheckNonExistentIrisResponse> checkNonExistentIrisExecutor;
    private final EntityLinearizationService linearizationService;
    private final CommandExecutor<ValidateLogicalDefinitionFromApiRequest, ValidateLogicalDefinitionFromApiResponse> validateLogicalDefinitionsExecutor;


    public ValidatorService(CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor,
                            CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor,
                            CommandExecutor<GetExistingClassesForApiRequest, GetExistingClassesForApiResponse> getEntitySearchExecutor,
                            CommandExecutor<ValidateEntityUpdateRequest, ValidateEntityUpdateResponse> validateEntityUpdateExecutor,
                            CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor,
                            CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> entityTypesExecutor,
                            CommandExecutor<CheckNonExistentIrisRequest, CheckNonExistentIrisResponse> checkNonExistentIrisExecutor,
                            CommandExecutor<ValidateLogicalDefinitionFromApiRequest, ValidateLogicalDefinitionFromApiResponse> validateLogicalDefinitionsExecutor,
                            EntityLinearizationService linearizationService) {
        this.isExistingProjectExecutor = isExistingProjectExecutor;
        this.filterExistingEntitiesExecutor = filterExistingEntitiesExecutor;
        this.getEntitySearchExecutor = getEntitySearchExecutor;
        this.validateEntityUpdateExecutor = validateEntityUpdateExecutor;
        this.tableConfigurationExecutor = tableConfigurationExecutor;
        this.entityTypesExecutor = entityTypesExecutor;
        this.checkNonExistentIrisExecutor = checkNonExistentIrisExecutor;
        this.linearizationService = linearizationService;
        this.validateLogicalDefinitionsExecutor = validateLogicalDefinitionsExecutor;
    }


    public void validateCreateEntityRequest(String projectId, CreateEntityDto createEntityDto) {
        validateTitle(createEntityDto.title());
        validateProjectId(projectId);
        validateEntityParents(projectId, createEntityDto.parent());
        validateExistingEntityName(createEntityDto.title(), projectId);
    }

    private void validateExistingEntityName(String entityName, String projectId) {
        try {
            Page<GetExistingClassesForApiResponse.ExistingClasses> resultPage = getEntitySearchExecutor.execute(new GetExistingClassesForApiRequest(ProjectId.valueOf(projectId),
                    entityName,
                    new HashSet<>(List.of(EntityType.CLASS)),
                    LangTagFilter.get(ImmutableSet.of(LangTag.get("en"))),
                    ImmutableList.of(),
                    PageRequest.requestFirstPage(), EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.CLASS)),

                            DeprecatedEntitiesTreatment.INCLUDE_DEPRECATED_ENTITIES),
                    SecurityContextHelper.getExecutionContext()).get(15, TimeUnit.SECONDS).existingClassesList();

            if(resultPage.getPageElements().stream().anyMatch(element -> element.browserText().equals(entityName))){
                throw new IllegalArgumentException("An entity with the same name already exists. Please choose a different name");
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    @Async
    private CompletableFuture<Boolean> isExistingProject(String projectId) {
        return isExistingProjectExecutor.execute(GetIsExistingProjectRequest.create(ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(GetIsExistingProjectResponse::isExistingProject);
    }

    @Async
    private CompletableFuture<Set<String>> getExistingEntities(String projectId, String entity) {
        var entityIri = IRI.create(entity);
        return filterExistingEntitiesExecutor.execute(FilterExistingEntitiesRequest.create(ProjectId.valueOf(projectId), ImmutableSet.of(entityIri)), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.existingEntities()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toSet())
                );
    }

    private void validateTitle(String title) {
        if(title == null || title.isBlank()){
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (hasEscapeCharacters(title)) {
            throw new IllegalArgumentException(MessageFormat.format("Title has escape characters: {0}. please remove any escape characters", title));
        }
    }

    public void validateProjectId(String projectId) {
        boolean projectExists;
        try {
            projectExists = this.isExistingProject(projectId).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if projectId:" + projectId + " is valid!", e);
            throw new RuntimeException(e);
        }
        if (!projectExists) {
            throw new IllegalArgumentException("Invalid Project ID: " + projectId);
        }
    }

    private void validateEntityParents(String projectId, String parent) {
        if (parent == null || parent.isEmpty()) {
            throw new IllegalArgumentException("At least a parent should be specified!");
        }
        Set<String> existingParents;
        try {
            existingParents = this.getExistingEntities(projectId, parent).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parent:" + parent + " is valid!", e);
            throw new RuntimeException(e);
        }
        boolean isValid = existingParents.stream().anyMatch(existingParent -> existingParent.equals(parent));
        if (!isValid) {
            throw new IllegalArgumentException("Invalid Entity Parent: " + parent);
        }
    }

    public void validateEntityExists(String projectId, String entity){
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("At least an entityUri should be specified!");
        }
        Set<String> existingEntities;
        try {
            existingEntities = this.getExistingEntities(projectId, entity).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parent:" + entity + " is valid!", e);
            throw new RuntimeException(e);
        }
        boolean isValid = existingEntities.stream().anyMatch(existingParent -> existingParent.equals(entity));
        if (!isValid) {
            throw new EntityIsMissingException("Invalid Entity IRI: " + entity);
        }
    }

    public void validateBaseExclusionTerms(List<BaseExclusionTerm> baseExclusionTerms) {
        if (baseExclusionTerms != null) {
            for (BaseExclusionTerm term : baseExclusionTerms) {
                validateBaseExclusionTerm(term);
            }
        }
    }

    private void validateBaseExclusionTerm(BaseExclusionTerm term) {
        if (term == null) {
            throw new IllegalArgumentException("BaseExclusionTerm cannot be null");
        }
        
        if (term.foundationReference() == null || term.foundationReference().trim().isEmpty()) {
            throw new IllegalArgumentException("BaseExclusionTerm has invalid foundationReference: cannot be null, empty, or blank");
        }
    }

    public void validateOWLEntityDto(OWLEntityDto owlEntityDto, String projectId) {
        if (owlEntityDto == null) {
            throw new IllegalArgumentException("OWLEntityDto cannot be null");
        }
        
        if (owlEntityDto.languageTerms() != null && owlEntityDto.languageTerms().baseExclusionTerms() != null) {
            validateBaseExclusionTerms(owlEntityDto.languageTerms().baseExclusionTerms());
        }

        validateTermIdsExistence(owlEntityDto, projectId);
        validateParentsExistence(owlEntityDto, projectId);

        if (owlEntityDto.postcoordination() != null) {
            validatePostcoordination(owlEntityDto, projectId);
        }
        if (owlEntityDto.logicalConditions() != null && owlEntityDto.logicalConditions().jsonRepresentation() != null) {
            LogicalConditions logicalConditions = owlEntityDto.logicalConditions().jsonRepresentation();
            validateLogicalDefinitions(projectId, owlEntityDto.entityIRI(), logicalConditions);
        }
    }

    private void validateParentsExistence(OWLEntityDto owlEntityDto, String projectId) {
        if (owlEntityDto.parents() == null || owlEntityDto.parents().isEmpty()) {
            return;
        }

        try {
            Set<IRI> parentIris = owlEntityDto.parents().stream()
                    .filter(parent -> parent != null && !parent.trim().isEmpty())
                    .map(IRI::create)
                    .collect(Collectors.toSet());

            if (parentIris.isEmpty()) {
                return;
            }

            Set<IRI> nonExistentIris = checkNonExistentIrisExecutor.execute(
                    new CheckNonExistentIrisRequest(ProjectId.valueOf(projectId), ImmutableSet.copyOf(parentIris)),
                    SecurityContextHelper.getExecutionContext()
            ).get().nonExistentIris();

            if (!nonExistentIris.isEmpty()) {
                List<String> missingParents = nonExistentIris.stream()
                        .map(IRI::toString)
                        .collect(Collectors.toList());
                throw new IllegalArgumentException("The following parent entities do not exist: " + String.join(", ", missingParents));
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parents are valid!", e);
            throw new RuntimeException("Error validating parents existence", e);
        }
    }

    private void validateTermIdsExistence(OWLEntityDto owlEntityDto, String projectId) {
        List<String> termIds = collectTermIds(owlEntityDto);

        if (termIds.isEmpty()) {
            return;
        }

        try {
            Set<IRI> termIdIris = termIds.stream()
                    .map(IRI::create)
                    .collect(Collectors.toSet());

            Set<IRI> nonExistentIris = checkNonExistentIrisExecutor.execute(
                    new CheckNonExistentIrisRequest(ProjectId.valueOf(projectId), ImmutableSet.copyOf(termIdIris)),
                    SecurityContextHelper.getExecutionContext()
            ).get().nonExistentIris();

            if (!nonExistentIris.isEmpty()) {
                List<String> missingTermIds = nonExistentIris.stream()
                        .map(IRI::toString)
                        .collect(Collectors.toList());
                throw new IllegalArgumentException("The following term IDs do not exist: " + String.join(", ", missingTermIds));
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if term IDs are valid!", e);
            throw new RuntimeException("Error validating term IDs existence", e);
        }
    }

    private List<String> collectTermIds(OWLEntityDto owlEntityDto) {
        List<String> termIds = new ArrayList<>();

        // Collection from languageTerms
        if (owlEntityDto.languageTerms() != null) {
            EntityLanguageTermsDto languageTerms = owlEntityDto.languageTerms();

            // Title termId
            if (languageTerms.title() != null && languageTerms.title().termId() != null && !languageTerms.title().termId().trim().isEmpty()) {
                termIds.add(languageTerms.title().termId());
            }

            // Definition termId
            if (languageTerms.definition() != null && languageTerms.definition().termId() != null && !languageTerms.definition().termId().trim().isEmpty()) {
                termIds.add(languageTerms.definition().termId());
            }

            // LongDefinition termId
            if (languageTerms.longDefinition() != null && languageTerms.longDefinition().termId() != null && !languageTerms.longDefinition().termId().trim().isEmpty()) {
                termIds.add(languageTerms.longDefinition().termId());
            }

            // FullySpecifiedName termId
            if (languageTerms.fullySpecifiedName() != null && languageTerms.fullySpecifiedName().termId() != null && !languageTerms.fullySpecifiedName().termId().trim().isEmpty()) {
                termIds.add(languageTerms.fullySpecifiedName().termId());
            }

            if(owlEntityDto.languageTerms().subclassBaseInclusions() != null && !owlEntityDto.languageTerms().subclassBaseInclusions().isEmpty()) {
                termIds.addAll(owlEntityDto.languageTerms().subclassBaseInclusions());
            }

            // BaseIndexTerms termIds and indexTypes
            if (languageTerms.baseIndexTerms() != null) {
                languageTerms.baseIndexTerms().stream()
                        .filter(Objects::nonNull)
                        .forEach(term -> {
                            if (term.termId() != null && !term.termId().trim().isEmpty()) {
                                termIds.add(term.termId());
                            }
                            if (term.indexType() != null && !term.indexType().trim().isEmpty()) {
                                termIds.add(term.indexType());
                            }
                        });
            }

            // BaseExclusionTerms termIds and foundationReferences
            if (languageTerms.baseExclusionTerms() != null) {
                languageTerms.baseExclusionTerms().stream()
                        .filter(term -> term != null)
                        .forEach(term -> {
                            if (term.termId() != null && !term.termId().trim().isEmpty()) {
                                termIds.add(term.termId());
                            }
                            if (term.foundationReference() != null && !term.foundationReference().trim().isEmpty()) {
                                termIds.add(term.foundationReference());
                            }
                        });
            }
        }

        // Collection from diagnosticCriteria
        if (owlEntityDto.diagnosticCriteria() != null && owlEntityDto.diagnosticCriteria().termId() != null && !owlEntityDto.diagnosticCriteria().termId().trim().isEmpty()) {
            termIds.add(owlEntityDto.diagnosticCriteria().termId());
        }

        // Collection from relatedImpairments
        if (owlEntityDto.relatedImpairments() != null) {
            owlEntityDto.relatedImpairments().stream()
                    .filter(term -> term != null && term.termId() != null && !term.termId().trim().isEmpty())
                    .forEach(term -> termIds.add(term.termId()));
        }

        return termIds;
    }

    private void validatePostcoordination(OWLEntityDto owlEntityDto, String projectId) {
        try {
            ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
            ProjectId projectIdObj = ProjectId.valueOf(projectId);
            String entityIri = owlEntityDto.entityIRI();

            // Mapping custom scales values
            WhoficCustomScalesValues customScalesValues = CustomScalesMapper.mapFromDtoList(
                    entityIri,
                    owlEntityDto.postcoordination().scaleCustomizations()
            );

            // Getting table configuration
            TableConfiguration tableConfiguration = tableConfigurationExecutor.execute(
                    new GetTablePostCoordinationAxisRequest(IRI.create(entityIri), projectIdObj),
                    executionContext
            ).get().tableConfiguration();

            // Getting entity types
            List<String> entityTypes = entityTypesExecutor.execute(
                    GetIcatxEntityTypeRequest.create(IRI.create(entityIri), projectIdObj),
                    executionContext
            ).get().icatxEntityTypes();

            // Getting definitions
            List<LinearizationDefinition> definitions = linearizationService.getDefinitionList(executionContext);

            // Mapping specification
            WhoficEntityPostCoordinationSpecification specification = SpecificationMapper.mapFromDtoList(
                    entityIri,
                    entityTypes.isEmpty() ? "ICD" : entityTypes.get(0),
                    owlEntityDto.postcoordination().postcoordinationSpecifications(),
                    definitions,
                    tableConfiguration
            );

            // Validation
            ValidateEntityUpdateResponse validationResponse = validateEntityUpdateExecutor.execute(
                    new ValidateEntityUpdateRequest(projectIdObj, customScalesValues, specification),
                    executionContext
            ).get();

            if (validationResponse.getErrorMessages() != null && !validationResponse.getErrorMessages().isEmpty()) {
                String errorMessage = String.join("; ", validationResponse.getErrorMessages());
                throw new ValidationException(errorMessage);
            }
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error validating postcoordination for entity " + owlEntityDto.entityIRI(), e);
            throw new RuntimeException("Error validating postcoordination for entity " + owlEntityDto.entityIRI(), e);
        }
    }
    public void validateLogicalDefinitions(String projectId, String entityIri, LogicalConditions logicalConditions) {
        if (logicalConditions == null) {
            throw new IllegalArgumentException("Logical conditions cannot be null");
        }

        // Map EntityLogicalDefinition to LogicalDefinition
        List<ValidateLogicalDefinitionFromApiRequest.LogicalDefinition> logicalDefinitions =
                logicalConditions.logicalDefinitions() != null ?
                        logicalConditions.logicalDefinitions().stream()
                                .map(ld -> new ValidateLogicalDefinitionFromApiRequest.LogicalDefinition(
                                        ld.logicalDefinitionSuperclass(),
                                        ld.relationships() != null ?
                                                ld.relationships().stream()
                                                        .map(rel -> new ValidateLogicalDefinitionFromApiRequest.Relationship(
                                                                rel.axis(),
                                                                rel.filler()
                                                        ))
                                                        .toList() :
                                                List.of()
                                ))
                                .toList() :
                        List.of();

        // Map LogicalConditionRelationship to Relationship
        List<ValidateLogicalDefinitionFromApiRequest.Relationship> necessaryConditions =
                logicalConditions.necessaryConditions() != null ?
                        logicalConditions.necessaryConditions().stream()
                                .map(rel -> new ValidateLogicalDefinitionFromApiRequest.Relationship(
                                        rel.axis(),
                                        rel.filler()
                                ))
                                .toList() :
                        List.of();

        try {
            ValidateLogicalDefinitionFromApiRequest request = new ValidateLogicalDefinitionFromApiRequest(
                    ProjectId.valueOf(projectId),
                    entityIri,
                    logicalDefinitions,
                    necessaryConditions
            );

            ValidateLogicalDefinitionFromApiResponse response = validateLogicalDefinitionsExecutor.execute(request, SecurityContextHelper.getExecutionContext())
                    .get(15, TimeUnit.SECONDS);

            if (response != null && response.messages() != null && !response.messages().isEmpty()) {
                String errorMessage = String.join("; ", response.messages());
                LOGGER.error("Logical definitions validation failed for entity: " + entityIri + ". Errors: " + errorMessage);
                throw new IllegalArgumentException("Logical definitions validation failed: " + errorMessage);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Error validating logical definitions for entity: " + entityIri, e);
            throw new RuntimeException("Error validating logical definitions: " + e.getMessage(), e);
        }
    }
    public static boolean hasEscapeCharacters(String input) {
        for (int i = 0; i < input.length() - 1; i++) {
            if (input.charAt(i) == '\\') {
                char nextChar = input.charAt(i + 1);
                if ("ntbrf\"'\\".indexOf(nextChar) != -1) {
                    return true;
                }
            }
        }
        return false;
    }
}
