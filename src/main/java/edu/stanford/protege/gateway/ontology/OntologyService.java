package edu.stanford.protege.gateway.ontology;


import com.fasterxml.jackson.databind.*;
import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.gateway.*;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.projects.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.semanticweb.owlapi.model.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OntologyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);
    private final CommandExecutor<GetEntityDirectParentsRequest, GetEntityDirectParentsResponse> directParentsExecutor;
    private final CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;
    private final CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;
    private final CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> updateLogicalDefinitionExecutor;
    private final CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> updateParentsExecutor;
    private final CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> updateLanguageTermsExecutor;
    private final CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;
    private final CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor;
    private final CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor;
    private final CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor;

    private final CommandExecutor<GetReproducibleProjectsRequest, GetReproducibleProjectsResponse> reproducibleProjectsExecutor;

    public OntologyService(CommandExecutor<GetEntityDirectParentsRequest, GetEntityDirectParentsResponse> directParentsExecutor,
                           CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor,
                           CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor,
                           CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor,
                           CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor,
                           CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor,
                           CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor,
                           CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> updateLogicalDefinitionExecutor,
                           CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> updateParentsExecutor,
                           CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> updateLanguageTermsExecutor, CommandExecutor<GetReproducibleProjectsRequest, GetReproducibleProjectsResponse> reproducibleProjectsExecutor) {
        this.directParentsExecutor = directParentsExecutor;
        this.logicalDefinitionExecutor = logicalDefinitionExecutor;
        this.formDataExecutor = formDataExecutor;
        this.updateLogicalDefinitionExecutor = updateLogicalDefinitionExecutor;
        this.updateParentsExecutor = updateParentsExecutor;
        this.updateLanguageTermsExecutor = updateLanguageTermsExecutor;
        this.entityChildrenExecutor = entityChildrenExecutor;
        this.createClassEntityExecutor = createClassEntityExecutor;
        this.getProjectsExecutor = getProjectsExecutor;
        this.entityDiscussionExecutor = entityDiscussionExecutor;
        this.reproducibleProjectsExecutor = reproducibleProjectsExecutor;
    }


    @Async
    public CompletableFuture<List<String>> getEntityParents(String entityIri, String projectId) {
        return getEntityParents(entityIri, projectId, SecurityContextHelper.getExecutionContext());
    }

    @Async
    public CompletableFuture<List<String>> getEntityParents(String entityIri, String projectId, ExecutionContext executionContext) {
        return directParentsExecutor.execute(new GetEntityDirectParentsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), executionContext)
                .thenApply(response ->
                        response.directParents().stream().map(child -> child.getEntity().getIRI().toString())
                                .sorted()
                                .collect(Collectors.toList()));

    }

    @Async
    public CompletableFuture<EntityLogicalConditionsWrapper> getEntityLogicalConditions(String entityIri, String projectId) {
        return getEntityLogicalConditions(entityIri, projectId, SecurityContextHelper.getExecutionContext());
    }

    @Async
    public CompletableFuture<EntityLogicalConditionsWrapper> getEntityLogicalConditions(String entityIri, String projectId, ExecutionContext executionContext) {
        return logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), executionContext)
                .thenApply(response ->
                        new EntityLogicalConditionsWrapper(new LogicalConditions(LogicalDefinitionMapper.mapToEntityLogicalDefinition(response.logicalDefinitions()),
                                LogicalDefinitionMapper.extractRelationshipsFromPropertyClassValue(response.necessaryConditions())),
                                new LogicalConditionsFunctionalOwl("OWLFunctionalSyntax", response.functionalAxioms()))
                );

    }

    @Async
    public CompletableFuture<EntityLanguageTerms> getEntityLanguageTerms(String entityIri, String projectId, String formId, ExecutionContext executionContext) {
        return formDataExecutor.execute(new GetEntityFormAsJsonRequest(ProjectId.valueOf(projectId), entityIri, formId), executionContext)
                .thenApply(formResponse -> EntityFormToDtoMapper.mapFormToTerms(formResponse.form()));

    }

    public void updateLogicalDefinition(String entityIri, String projectId, EntityLogicalConditionsWrapper logicalConditionsWrapper, ChangeRequestId changeRequestId) {
        try {
            GetLogicalDefinitionsResponse response = logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), SecurityContextHelper.getExecutionContext())
                    .get();

            OntologicalLogicalDefinitionConditions pristine = new OntologicalLogicalDefinitionConditions(response.logicalDefinitions(), response.necessaryConditions());
            UpdateLogicalDefinitionsRequest request = UpdateLogicalDefinitionsRequest.create(changeRequestId,
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


    public void updateEntityParents(String entityIri, String projectId, List<String> parents, ChangeRequestId changeRequestId) {
        ImmutableSet<OWLClass> parentsAsClass = ImmutableSet.copyOf(parents.stream().map(p -> new OWLClassImpl(IRI.create(p))).collect(Collectors.toList()));

        try {
            updateParentsExecutor.execute(new ChangeEntityParentsRequest(changeRequestId,
                    ProjectId.valueOf(projectId),
                    parentsAsClass,
                    new OWLClassImpl(IRI.create(entityIri)),
                    "Update parents through API"), SecurityContextHelper.getExecutionContext()).get();
        } catch (Exception e) {
            LOGGER.error("Error updating entity parents for entity " + entityIri, e);
            throw new ApplicationException("Error updating entity parents for entity " + entityIri);
        }
    }

    public void updateLanguageTerms(String entityIri, String projectId, String formId, OWLEntityDto owlEntityDto, ChangeRequestId changeRequestId) {
        try {
            ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
            EntityLanguageTerms languageTerms = EntityLanguageTerms.getFromLanguageTermDto(owlEntityDto.languageTerms(), owlEntityDto.isObsolete(), owlEntityDto.diagnosticCriteria());
            updateLanguageTermsExecutor.execute(
                    new SetEntityFormDataFromJsonRequest(
                            changeRequestId,
                            ProjectId.valueOf(projectId),
                            new OWLClassImpl(IRI.create(entityIri)),
                            formId,
                            objectMapper.convertValue(EntityFormToDtoMapper.mapFromDto(entityIri, languageTerms), JsonNode.class)
                    ),
                    SecurityContextHelper.getExecutionContext()
            ).get();
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

    public CompletableFuture<List<ReproducibleProject>> getReproducibleProjects() {
        return reproducibleProjectsExecutor.execute(new GetReproducibleProjectsRequest(), SecurityContextHelper.getExecutionContext())
                .thenApply(GetReproducibleProjectsResponse::reproducibleProjectList);
    }

    public CompletableFuture<EntityComments> getEntityDiscussionThreads(String entityIri, String projectId) {
        return entityDiscussionExecutor.execute(GetEntityCommentsRequest.create(ProjectId.valueOf(projectId), entityIri), SecurityContextHelper.getExecutionContext())
                .thenApply(GetEntityCommentsResponse::comments);
    }


}
