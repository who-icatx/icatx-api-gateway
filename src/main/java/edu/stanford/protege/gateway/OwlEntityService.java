package edu.stanford.protege.gateway;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ExecutionException;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final OntologyService ontologyService;

    private final EntityHistoryService entityHistoryService;

    @Value("${icatx.formId}")
    private String formId;


    public OwlEntityService(EntityLinearizationService entityLinearizationService,
                            EntityPostCoordinationService entityPostCoordinationService,
                            EntityHistoryService entityHistoryService,
                            OntologyService ontologyService) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.ontologyService = ontologyService;
        this.entityHistoryService = entityHistoryService;
    }


    public OWLEntityDto getEntityInfo(String entityIri, String projectId) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, projectId);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = ontologyService.getEntityLanguageTerms(entityIri, projectId, this.formId);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = ontologyService.getEntityLogicalConditions(entityIri, projectId);
        CompletableFuture<List<String>> parents = ontologyService.getEntityParents(entityIri, projectId);
        CompletableFuture<LocalDateTime> latestChange = entityHistoryService.getEntityLatestChangeTime(projectId, entityIri);
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
            if (terms == null || terms.title() == null || terms.title().label() == null || terms.title().label().isEmpty()) {
                throw new EntityIsMissingException("Entity with iri " + entityIri + " is missing");
            }
            return new OWLEntityDto(entityIri,
                    entityLanguageTerms.get(),
                    linearizationDto.get(),
                    new EntityPostCoordinationWrapperDto(specList.get(), new Date(), customScalesDtos.get()),
                    latestChange.get(),
                    logicalConditions.get(),
                    parents.get()
            );
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new ApplicationException("Error fetching data for entity " + entityIri);
        }

    }

    public List<String> getEntityChildren(String entityIRI, String projectId) {
        CompletableFuture<List<String>> entityChildren = ontologyService.getEntityChildren(entityIRI, projectId);

        try {
            return entityChildren.get();
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIRI, e);
            throw new RuntimeException(e);
        }
    }

    public Set<String> createClassEntity(String projectId, CreateEntityDto createEntityDto) {
        CompletableFuture<Set<String>> newCreatedEntityIri = ontologyService.createClassEntity(projectId, createEntityDto);
        try {
            return newCreatedEntityIri.get();
        } catch (Exception e) {
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
        try {
            return availableProjects.get();
        } catch (Exception e) {
            LOGGER.error("Error retrieving available projects!", e);
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
        validateEntityUpdate(owlEntityDto, existingProjectId, callerHash);

        ProjectId projectId = ProjectId.valueOf(existingProjectId);
        entityLinearizationService.updateEntityLinearization(owlEntityDto, projectId);
        entityPostCoordinationService.updateEntityPostCoordination(owlEntityDto.postcoordination(), projectId, owlEntityDto.entityIRI());
        ontologyService.updateLogicalDefinition(owlEntityDto.entityIRI(), existingProjectId, owlEntityDto.logicalConditions());
        ontologyService.updateEntityParents(owlEntityDto.entityIRI(), existingProjectId, owlEntityDto.parents());
        ontologyService.updateLanguageTerms(owlEntityDto.entityIRI(), existingProjectId, this.formId, owlEntityDto.languageTerms());
        return getEntityInfo(owlEntityDto.entityIRI(), existingProjectId);

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
            if (latestChange.equals(LocalDateTime.MIN)) {
                throw new EntityIsMissingException("Entity with iri " + owlEntityDto.entityIRI() + " is missing");
            }
            String etag = Hashing.sha256().hashString(latestChange.toString(), StandardCharsets.UTF_8).toString();

            if (callerHash != null && !callerHash.replace("\"", "").equals(etag)) {
                throw new VersionDoesNotMatchException("Received hash " + callerHash + " is different from " + etag);
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
}
