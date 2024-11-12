package edu.stanford.protege.gateway;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.EntityOntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class OwlEntityService {
    private final Logger LOGGER = LoggerFactory.getLogger(OwlEntityService.class);

    private final EntityLinearizationService entityLinearizationService;

    private final EntityPostCoordinationService entityPostCoordinationService;

    private final EntityOntologyService entityOntologyService;


    @Value("${icatx.projectId}")
    private String existingProjectId;

    @Value("${icatx.formId}")
    private String formId;


    public OwlEntityService(EntityLinearizationService entityLinearizationService, EntityPostCoordinationService entityPostCoordinationService, EntityOntologyService entityOntologyService) {
        this.entityLinearizationService = entityLinearizationService;
        this.entityPostCoordinationService = entityPostCoordinationService;
        this.entityOntologyService = entityOntologyService;
    }


    public OWLEntityDto getEntityInfo(String entityIri) {
        CompletableFuture<EntityLinearizationWrapperDto> linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, this.existingProjectId);
        CompletableFuture<List<EntityPostCoordinationSpecificationDto>> specList = entityPostCoordinationService.getPostCoordinationSpecifications(entityIri, this.existingProjectId);
        CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> customScalesDtos = entityPostCoordinationService.getEntityCustomScales(entityIri, this.existingProjectId);
        CompletableFuture<EntityLanguageTerms> entityLanguageTerms = entityOntologyService.getEntityLanguageTerms(entityIri, this.existingProjectId, this.formId);
        CompletableFuture<EntityLogicalConditionsWrapper> logicalConditions = entityOntologyService.getEntityLogicalConditions(entityIri, this.existingProjectId);
        CompletableFuture<List<String>> parents = entityOntologyService.getEntityParents(entityIri, this.existingProjectId);

        CompletableFuture<Void> combinedFutures = CompletableFuture.allOf(linearizationDto, specList, customScalesDtos, entityLanguageTerms, logicalConditions, parents);
        combinedFutures.join();

        try{
            return new OWLEntityDto(entityIri,
                    entityLanguageTerms.get(),
                    linearizationDto.get(),
                    new EntityPostCoordinationWrapperDto(specList.get(),new Date(), customScalesDtos.get()),
                    new Date(),
                    logicalConditions.get(),
                    parents.get()
            );
        } catch (Exception e){
            LOGGER.error("Error fetching data for entity " + entityIri, e);
            throw new RuntimeException(e);
        }
    }

    public OWLEntityDto updateEntity(OWLEntityDto owlEntityDto) {
        try {
            ProjectId projectId = ProjectId.valueOf(this.existingProjectId);
            entityLinearizationService.updateEntityLinearization(owlEntityDto, projectId );
            entityPostCoordinationService.updateEntityPostCoordination(owlEntityDto.postcoordination(), projectId, owlEntityDto.entityIRI());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return owlEntityDto;
    }
}
