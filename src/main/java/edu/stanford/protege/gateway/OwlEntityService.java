package edu.stanford.protege.gateway;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.EntityOntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final EntityOntologyService entityOntologyService;

    @Nonnull
    private final EventDispatcher eventDispatcher;

    @Value("${icatx.formId}")
    private String formId;


    public OwlEntityService(EntityLinearizationService entityLinearizationService, EntityPostCoordinationService entityPostCoordinationService, EntityOntologyService entityOntologyService, @Nonnull EventDispatcher eventDispatcher) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.entityOntologyService = entityOntologyService;
        this.eventDispatcher = eventDispatcher;
    }


    public OWLEntityDto getEntityInfo(String entityIri, String projectId) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, projectId);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, projectId);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = entityOntologyService.getEntityLanguageTerms(entityIri, projectId, this.formId);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = entityOntologyService.getEntityLogicalConditions(entityIri, projectId);
        CompletableFuture<List<String>> parents = entityOntologyService.getEntityParents(entityIri, projectId);

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
        CompletableFuture<List<String>> entityChildren = entityOntologyService.getEntityChildren(entityIri, projectId);

        try {
            return entityChildren.get();
        } catch (Exception e) {
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }
    }

    public OWLEntityDto updateEntity(OWLEntityDto owlEntityDto, String existingProjectId) {
        ChangeRequestId changeRequestId = ChangeRequestId.generate();
        ProjectId projectId = ProjectId.valueOf(existingProjectId);

        try {
            entityLinearizationService.updateEntityLinearization(owlEntityDto, projectId, changeRequestId);
            entityPostCoordinationService.updateEntityPostCoordination(owlEntityDto.postcoordination(), projectId, owlEntityDto.entityIRI(), changeRequestId);
/*            entityOntologyService.updateEntityParents(owlEntityDto.entityIRI(), existingProjectId, owlEntityDto.parents(), changeRequestId);
            entityOntologyService.updateLanguageTerms(owlEntityDto.entityIRI(), existingProjectId, this.formId, owlEntityDto.languageTerms(), changeRequestId);*/
            eventDispatcher.dispatchEvent(new EntityUpdatedSuccessfullyEvent(projectId, EventId.generate(), owlEntityDto.entityIRI(), changeRequestId));
        } catch (Exception e) {
            LOGGER.error("Error updating entity ", e);
            eventDispatcher.dispatchEvent(new EntityUpdateFailedEvent(projectId, EventId.generate(), owlEntityDto.entityIRI(), changeRequestId));
            throw new RuntimeException(e);
        }
       return getEntityInfo(owlEntityDto.entityIRI(), existingProjectId);
    }
}
