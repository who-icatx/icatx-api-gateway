package edu.stanford.protege.gateway;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final OntologyService ontologyService;


    @Value("${icatx.formId}")
    private String formId;


    public OwlEntityService(EntityLinearizationService entityLinearizationService, EntityPostCoordinationService entityPostCoordinationService, OntologyService ontologyService) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.ontologyService = ontologyService;
    }


    public OWLEntityDto getEntityInfo(String entityIri, String projectId) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, projectId);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = ontologyService.getEntityLanguageTerms(entityIri, projectId, this.formId);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = ontologyService.getEntityLogicalConditions(entityIri, projectId);
        CompletableFuture<List<String>> parents = ontologyService.getEntityParents(entityIri, projectId);

        CompletableFuture<Void> combinedFutures = CompletableFuture.allOf(linearizationDto, specList, customScalesDtos, entityLanguageTerms, logicalConditions, parents);
        combinedFutures.join();

        try {
            return new OWLEntityDto(entityIri,
                    entityLanguageTerms.get(),
                    linearizationDto.get(),
                    new EntityPostCoordinationWrapperDto(specList.get(), new Date(), customScalesDtos.get()),
                    new Date(),
                    logicalConditions.get(),
                    parents.get()
            );
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }

    }

    public List<String> getEntityChildren(String entityIri, String projectId) {
        CompletableFuture<List<String>> entityChildren = ontologyService.getEntityChildren(entityIri, projectId);

        try {
            return entityChildren.get();
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }
    }

    public Set<String> createClassEntity(String projectId, CreateEntityDto createEntityDto) {
        CompletableFuture<Set<String>> newCreatedEntityIri = ontologyService.createClassEntity(projectId, createEntityDto);
        try {
            return newCreatedEntityIri.get();
        } catch (Exception e) {
            LOGGER.error("Error creating new class entity " + createEntityDto.entityName(), e);
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
}
