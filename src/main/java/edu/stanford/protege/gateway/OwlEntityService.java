package edu.stanford.protege.gateway;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.gateway.projects.ReproducibleProject;
import edu.stanford.protege.gateway.validators.ValidatorService;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final OntologyService ontologyService;
    @Nonnull
    private final EventDispatcher eventDispatcher;
    private final EntityHistoryService entityHistoryService;

    @Value("${icatx.formId}")
    private String formId;

    private final ValidatorService validatorService;


    public OwlEntityService(EntityLinearizationService entityLinearizationService,
                            EntityPostCoordinationService entityPostCoordinationService,
                            EntityHistoryService entityHistoryService,
                            @Nonnull EventDispatcher eventDispatcher,
                            OntologyService ontologyService,
                            ValidatorService validatorService) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.ontologyService = ontologyService;
        this.eventDispatcher = eventDispatcher;
        this.entityHistoryService = entityHistoryService;
        this.validatorService = validatorService;
    }

    public OWLEntityDto getEntityInfo(String entityIri, String projectId) {
        validatorService.validateProjectId(projectId);
        validatorService.validateEntityExists(projectId, entityIri);
        return getEntityInfo(entityIri, projectId, SecurityContextHelper.getExecutionContext());
    }

    public OWLEntityDto getEntityInfo(String entityIri, String projectId, ExecutionContext executionContext) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, projectId, executionContext);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, projectId, executionContext);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, projectId, executionContext);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = ontologyService.getEntityLanguageTerms(entityIri, projectId, this.formId, executionContext);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = ontologyService.getEntityLogicalConditions(entityIri, projectId, executionContext);
        CompletableFuture<List<String>> parents = ontologyService.getEntityParents(entityIri, projectId, executionContext);
        CompletableFuture<LocalDateTime> latestChange = entityHistoryService.getEntityLatestChangeTime(projectId, entityIri, executionContext);
        CompletableFuture<Void> combinedFutures = CompletableFuture.allOf(linearizationDto,
                specList,
                customScalesDtos,
                entityLanguageTerms,
                logicalConditions,
                parents,
                latestChange);
        combinedFutures.join();

        try {
            EntityLanguageTerms terms = entityLanguageTerms.get();
            EntityLanguageTermsDto termsDto = EntityLanguageTermsDto.getFromTerms(terms);
            if (terms == null || terms.title() == null || terms.title().label() == null || terms.title().label().isEmpty()) {
                throw new EntityIsMissingException("Entity with iri " + entityIri + " is missing");
            }
            return new OWLEntityDto(entityIri,
                    terms.isObsolete(),
                    termsDto,
                    latestChange.get(),
                    linearizationDto.get(),
                    new EntityPostCoordinationWrapperDto(specList.get(), new Date(), customScalesDtos.get()),
                    logicalConditions.get(),
                    parents.get()
            );
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new ApplicationException("Error fetching data for entity " + entityIri);
        }

    }

    public List<String> getEntityChildren(String entityIRI, String projectId) {
        validatorService.validateProjectId(projectId);
        validatorService.validateEntityExists(projectId, entityIRI);
        CompletableFuture<List<String>> entityChildren = ontologyService.getEntityChildren(entityIRI, projectId);

        try {
            return entityChildren.get();
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIRI, e);
            throw new RuntimeException(e);
        }
    }

    public String createClassEntity(String projectId, CreateEntityDto createEntityDto) {
        validatorService.validateCreateEntityRequest(projectId, createEntityDto);
        CompletableFuture<String> newCreatedEntityIri = ontologyService.createClassEntity(projectId, createEntityDto);
        try {
            return newCreatedEntityIri.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error creating new class entity " + createEntityDto.title(), e);
            /*
            ToDo:
                Here we can add the revert event for all the services if the creation fails for whatever reason.
             */
            throw new RuntimeException(e);
        }
    }

    public Set<ProjectSummaryDto> getProjects() {
        CompletableFuture<Set<ProjectSummaryDto>> availableProjects = ontologyService.getProjects();
        CompletableFuture<List<ReproducibleProject>> allReproducibleProjects = ontologyService.getReproducibleProjects();
        CompletableFuture<Void> combinedFutures = CompletableFuture.allOf(availableProjects, allReproducibleProjects);
        combinedFutures.join();
        try {
            List<ReproducibleProject> reproducibleProjectList = allReproducibleProjects.get();
            return availableProjects.get().stream().map(dto -> new ProjectSummaryDto(dto.projectId(),
                    dto.title(),
                    dto.createdAt(),
                    dto.description(),
                    getReproducibleProject(reproducibleProjectList, dto.projectId())
                            .map(ReproducibleProject::associatedBranch).orElse("unknownBranch")
                    )).collect(Collectors.toSet());
        } catch (Exception e) {
            LOGGER.error("Error retrieving available projects !", e);
            throw new RuntimeException(e);
        }
    }

    public EntityComments getEntityComments(String entityIri, String projectId) {
        try {
            return ontologyService.getEntityDiscussionThreads(entityIri, projectId).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching data for entity discussion threads for entity" + entityIri, e);
            throw new RuntimeException(e);
        }
    }


    public OWLEntityDto updateEntity(OWLEntityDto owlEntityDto, String existingProjectId, String callerHash) {
        ChangeRequestId changeRequestId = ChangeRequestId.generate();
        ProjectId projectId = ProjectId.valueOf(existingProjectId);

        try {
            validateEntityUpdate(owlEntityDto, existingProjectId, callerHash);
            entityLinearizationService.updateEntityLinearization(owlEntityDto, projectId, changeRequestId);
            entityPostCoordinationService.updateEntityPostCoordination(owlEntityDto.postcoordination(), projectId, owlEntityDto.entityIRI(), changeRequestId);
            ontologyService.updateLogicalDefinition(owlEntityDto.entityIRI(), existingProjectId, owlEntityDto.logicalConditions(), changeRequestId);
            ontologyService.updateEntityParents(owlEntityDto.entityIRI(), existingProjectId, owlEntityDto.parents(), changeRequestId);
            ontologyService.updateLanguageTerms(owlEntityDto.entityIRI(), existingProjectId, this.formId, owlEntityDto, changeRequestId);
            eventDispatcher.dispatchEvent(new EntityUpdatedSuccessfullyEvent(projectId, EventId.generate(), owlEntityDto.entityIRI(), changeRequestId), SecurityContextHelper.getExecutionContext());
            return getEntityInfo(owlEntityDto.entityIRI(), existingProjectId);

        } catch (ApplicationException e) {
            LOGGER.error("Error updating entity ", e);
            eventDispatcher.dispatchEvent(new EntityUpdateFailedEvent(projectId, EventId.generate(), owlEntityDto.entityIRI(), changeRequestId), SecurityContextHelper.getExecutionContext());
            throw e;
        }
    }

    private void validateEntityUpdate(OWLEntityDto owlEntityDto, String existingProjectId, String callerHash) {

        callerVersionMatchesLatestVersion(owlEntityDto, existingProjectId, callerHash);
        entityIsNotItsOwnParent(owlEntityDto);
        linearizationParentsAreOnlyDirectParents(owlEntityDto, existingProjectId);
    }

    private void callerVersionMatchesLatestVersion(OWLEntityDto owlEntityDto, String existingProjectId, String callerHash) {
        try {
            LocalDateTime latestChange = entityHistoryService.getEntityLatestChangeTime(existingProjectId, owlEntityDto.entityIRI())
                    .get();
            if (!latestChange.equals(LocalDateTime.MIN)) {
                String etag = Hashing.sha256().hashString(latestChange.toString(), StandardCharsets.UTF_8).toString();

                if (callerHash != null && !callerHash.replace("\"", "").equals(etag)) {
                    throw new VersionDoesNotMatchException("Received version out of date : Received hash " + callerHash + " is different from " + etag);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching latest change for validation for entity " + owlEntityDto.entityIRI(), e);
            throw new ApplicationException("Error fetching latest change for validation for entity " + owlEntityDto.entityIRI());
        }
    }

    private void linearizationParentsAreOnlyDirectParents(OWLEntityDto owlEntityDto, String existingProjectId) {
        try {
            List<String> parents = ontologyService.getEntityParents(owlEntityDto.entityIRI(), existingProjectId).get();
            if (owlEntityDto.entityLinearizations() != null && owlEntityDto.entityLinearizations().linearizations() != null) {
                Optional<String> wrongLinearizationParent = owlEntityDto.entityLinearizations().linearizations().stream()
                        .map(EntityLinearization::linearizationPathParent)
                        .filter(pathParent -> pathParent != null && !pathParent.isEmpty())
                        .filter(parent -> !parents.contains(parent))
                        .findFirst();
                if (wrongLinearizationParent.isPresent()) {
                    throw new ValidationException("Entity has a linearization with parent " + wrongLinearizationParent.get() + " that is not in the available parents " + Arrays.toString(parents.toArray()));
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching parents for validation for entity " + owlEntityDto.entityIRI(), e);
            throw new ApplicationException("Error fetching parents for validation" + owlEntityDto.entityIRI());
        }

    }

    private void entityIsNotItsOwnParent(OWLEntityDto owlEntityDto) {

        if (owlEntityDto.parents().contains(owlEntityDto.entityIRI())) {
            throw new ValidationException("Entity contains in the parents its own parents");
        }

    }

    private Optional<ReproducibleProject> getReproducibleProject(List<ReproducibleProject> availableProjects, String projectId){
        return availableProjects.stream().filter(p -> p.projectId().equals(projectId)).findFirst();
    }
}
