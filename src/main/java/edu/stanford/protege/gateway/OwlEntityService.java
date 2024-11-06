package edu.stanford.protege.gateway;


import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationWrapperDto;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.EntityOntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Service
public class OwlEntityService {

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
        EntityLinearizationWrapperDto linearizationDto = entityLinearizationService.getEntityLinearizationDto(entityIri, this.existingProjectId);
        EntityPostCoordinationWrapperDto postcoordinationDto = entityPostCoordinationService.getEntityPostCoordination(entityIri, this.existingProjectId);
        Date lastChangeDate = MappingHelper.getClosestToTodayDate(Arrays.asList(linearizationDto.lastRevisionDate(), postcoordinationDto.lastRevisionDate()));
        return new OWLEntityDto(entityIri,
                entityOntologyService.getEntityLanguageTerms(entityIri, this.existingProjectId, this.formId),
                linearizationDto,
                postcoordinationDto,
                lastChangeDate,
                entityOntologyService.getEntityLogicalConditions(entityIri, this.existingProjectId),
                entityOntologyService.getEntityParents(entityIri, this.existingProjectId)
                );
    }
}
