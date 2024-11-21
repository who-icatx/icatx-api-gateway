package edu.stanford.protege.gateway;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.EntityOntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final EntityOntologyService entityOntologyService;

    private final EntityHistoryService entityHistoryService;

    @Value("${icatx.formId}")
    private String formId;


    public OwlEntityService(EntityLinearizationService entityLinearizationService, EntityPostCoordinationService entityPostCoordinationService, EntityOntologyService entityOntologyService, EntityHistoryService entityHistoryService) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.entityOntologyService = entityOntologyService;
        this.entityHistoryService = entityHistoryService;
    }


    public OWLEntityDto getEntityInfo(String entityIri, String projectId) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, projectId);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = entityOntologyService.getEntityLanguageTerms(entityIri, projectId, this.formId);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = entityOntologyService.getEntityLogicalConditions(entityIri, projectId);
        CompletableFuture<List<String>> parents = entityOntologyService.getEntityParents(entityIri, projectId);
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
            return new OWLEntityDto(entityIri,
                    entityLanguageTerms.get(),
                    linearizationDto.get(),
                    new EntityPostCoordinationWrapperDto(specList.get(), new Date(), customScalesDtos.get()),
                    latestChange.get(),
                    logicalConditions.get(),
                    parents.get()
            );
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }

    }

    public List<String> getEntityChildren(String entityIri, String projectId) {
        CompletableFuture<List<String>> entityChildren = entityOntologyService.getEntityChildren(entityIri, projectId);

        try {
            return entityChildren.get();
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }
    }
}
