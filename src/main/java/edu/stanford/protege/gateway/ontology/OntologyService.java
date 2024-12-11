package edu.stanford.protege.gateway.ontology;


import com.fasterxml.jackson.databind.*;
import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.gateway.*;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OntologyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);
    private final CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor;
    private final CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;
    private final CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;
    private final CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> updateLogicalDefinitionExecutor;
    private final CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> updateParentsExecutor;
    private final CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> updateLanguageTermsExecutor;

    private final CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;
    private final CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;
    private final CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;
    private final CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor;
    private final CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor;
    private final CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor;


    public OntologyService(CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor,
                           CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor,
                           CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor,
                           CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor,
                           CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor,
                           CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor,
                           CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor,
                           CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor,
                           CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor,
                           CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> updateLogicalDefinitionExecutor,
                           CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> updateParentsExecutor,
                           CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> updateLanguageTermsExecutor) {
        this.ancestorsExecutor = ancestorsExecutor;
        this.logicalDefinitionExecutor = logicalDefinitionExecutor;
        this.formDataExecutor = formDataExecutor;
        this.updateLogicalDefinitionExecutor = updateLogicalDefinitionExecutor;
        this.updateParentsExecutor = updateParentsExecutor;
        this.updateLanguageTermsExecutor = updateLanguageTermsExecutor;
        this.entityChildrenExecutor = entityChildrenExecutor;
        this.isExistingProjectExecutor = isExistingProjectExecutor;
        this.filterExistingEntitiesExecutor = filterExistingEntitiesExecutor;
        this.createClassEntityExecutor = createClassEntityExecutor;
        this.getProjectsExecutor = getProjectsExecutor;
        this.entityDiscussionExecutor = entityDiscussionExecutor;
    }


    public CompletableFuture<List<String>> getEntityParents(String entityIri, String projectId) {

        return ancestorsExecutor.execute(new GetClassAncestorsRequest(IRI.create(entityIri), ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(response ->
                        response.getAncestorClassHierarchy().getChildren().stream().map(child -> child.getNode().getEntity().getIRI().toString())
                                .collect(Collectors.toList()));

    }

    public CompletableFuture<EntityLogicalConditionsWrapper> getEntityLogicalConditions(String entityIri, String projectId) {
        return logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), SecurityContextHelper.getExecutionContext())
                .thenApply(response ->
                        new EntityLogicalConditionsWrapper(new LogicalConditions(LogicalDefinitionMapper.mapToEntityLogicalDefinition(response.logicalDefinitions()),
                                LogicalDefinitionMapper.extractRelationshipsFromPropertyClassValue(response.necessaryConditions())),
                                new LogicalConditionsFunctionalOwl("OWLFunctionalSyntax", response.functionalAxioms()))
                );

    }

    public CompletableFuture<EntityLanguageTerms> getEntityLanguageTerms(String entityIri, String projectId, String formId) {
        return formDataExecutor.execute(new GetEntityFormAsJsonRequest(ProjectId.valueOf(projectId), entityIri, formId), SecurityContextHelper.getExecutionContext())
                .thenApply(formResponse -> EntityFormToDtoMapper.mapFormToTerms(formResponse.form()));

    }

    public void updateLogicalDefinition(String entityIri, String projectId, EntityLogicalConditionsWrapper logicalConditionsWrapper) {
        try {
            GetLogicalDefinitionsResponse response = logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), SecurityContextHelper.getExecutionContext())
                    .get();

            OntologicalLogicalDefinitionConditions pristine = new OntologicalLogicalDefinitionConditions(response.logicalDefinitions(), response.necessaryConditions());
            UpdateLogicalDefinitionsRequest request = UpdateLogicalDefinitionsRequest.create(ChangeRequestId.generate(),
                    ProjectId.valueOf(projectId),
                    new OWLClassImpl(IRI.create(entityIri)),
                    pristine,
                    LogicalDefinitionMapper.mapFromDto(logicalConditionsWrapper.jsonRepresentation()),
                    "Update from API");

            updateLogicalDefinitionExecutor.execute(request, SecurityContextHelper.getExecutionContext()).get();

        } catch (Exception e) {
            LOGGER.error("Error updating logical definition for entity " + entityIri, e);
            throw new ApplicationException("Error updating logical definition for entity " + entityIri);
        }
    }


    public void updateEntityParents(String entityIri, String projectId, List<String> parents) {
        ImmutableSet<OWLClass> parentsAsClass = ImmutableSet.copyOf(parents.stream().map(p -> new OWLClassImpl(IRI.create(p))).collect(Collectors.toList()));

        try {
            updateParentsExecutor.execute(new ChangeEntityParentsRequest(ChangeRequestId.generate(),
                    ProjectId.valueOf(projectId),
                    parentsAsClass,
                    new OWLClassImpl(IRI.create(entityIri)),
                    "Update parents through API"), SecurityContextHelper.getExecutionContext()).get();
        } catch (Exception e) {
            LOGGER.error("Error updating entity parents for entity " + entityIri, e);
            throw new ApplicationException("Error updating entity parents for entity " + entityIri);
        }
    }

    public void updateLanguageTerms(String entityIri, String projectId, String formId, EntityLanguageTerms languageTerms) {
        try {
            ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
            updateLanguageTermsExecutor.execute(new SetEntityFormDataFromJsonRequest(ChangeRequestId.generate(),
                            ProjectId.valueOf(projectId),
                            new OWLClassImpl(IRI.create(entityIri)),
                            formId,
                            objectMapper.convertValue(EntityFormToDtoMapper.mapFromDto(entityIri, languageTerms), JsonNode.class)),
                    SecurityContextHelper.getExecutionContext()).get();
        } catch (Exception e) {
            LOGGER.error("Error updating updateLanguageTerms entity " + entityIri, e);
            throw new ApplicationException("Error updating updateLanguageTerms entity " + entityIri);
        }
    }

    public CompletableFuture<List<String>> getEntityChildren(String entityIri, String projectId) {
        return entityChildrenExecutor.execute(GetEntityChildrenRequest.create(IRI.create(entityIri), ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.childrenIris()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toList())
                );
    }

    public CompletableFuture<Boolean> isExistingProject(String projectId) {
        return isExistingProjectExecutor.execute(GetIsExistingProjectRequest.create(ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(GetIsExistingProjectResponse::isExistingProject);
    }

    public CompletableFuture<Set<String>> getExistingEntities(String projectId, String parent) {
        var parentIri = IRI.create(parent);
        return filterExistingEntitiesExecutor.execute(FilterExistingEntitiesRequest.create(ProjectId.valueOf(projectId), ImmutableSet.of(parentIri)), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.existingEntities()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toSet())
                );
    }

    public CompletableFuture<String> createClassEntity(String projectId, CreateEntityDto createEntityDto) {
        return createClassEntityExecutor.execute(
                CreateClassesFromApiRequest.create(
                        ChangeRequestId.generate(),
                        ProjectId.valueOf(projectId),
                        createEntityDto.title(),
                        createEntityDto.languageTag(),
                        createEntityDto.parent()
                ),
                SecurityContextHelper.getExecutionContext()
        ).thenApply(
                CreateClassesFromApiResponse::newEntityIri
        );
    }

    public CompletableFuture<Set<ProjectSummaryDto>> getProjects() {
        return getProjectsExecutor.execute(
                GetAvailableProjectsForApiRequest.create(),
                SecurityContextHelper.getExecutionContext()
        ).thenApply(
                response -> new HashSet<>(response.availableProjects())
        );
    }

    public CompletableFuture<EntityComments> getEntityDiscussionThreads(String entityIri, String projectId) {
        return entityDiscussionExecutor.execute(GetEntityCommentsRequest.create(ProjectId.valueOf(projectId), entityIri), SecurityContextHelper.getExecutionContext())
                .thenApply(GetEntityCommentsResponse::comments);
    }
}
