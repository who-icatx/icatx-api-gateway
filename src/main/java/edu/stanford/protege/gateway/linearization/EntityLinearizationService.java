package edu.stanford.protege.gateway.linearization;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityLinearization;
import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.dto.LinearizationTitle;
import edu.stanford.protege.gateway.linearization.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EntityLinearizationService {

    private final Logger LOGGER = LoggerFactory.getLogger(EntityLinearizationService.class);

    private final CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> entityLinearizationCommand;

    private final CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> definitionExecutor;

    public EntityLinearizationService(CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> entityLinearizationCommand, CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> definitionExecutor) {
        this.entityLinearizationCommand = entityLinearizationCommand;
        this.definitionExecutor = definitionExecutor;
    }


    public EntityLinearizationWrapperDto getEntityLinearizationDto(String entityIri, String projectId) {
        try {
            GetEntityLinearizationsResponse response = entityLinearizationCommand.execute(new GetEntityLinearizationsRequest(entityIri, ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                    .get();

            return mapFromResponse(response.linearizationSpecification(), response.lastRevisionDate());

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error fetching linearization of entity " + entityIri);
            throw new RuntimeException(e);
        }
    }

    public List<LinearizationDefinition> getDefinitionList(){
        try {
            return definitionExecutor.execute(new LinearizationDefinitionRequest(), SecurityContextHelper.getExecutionContext()).get().definitionList();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Unrecoverable error while fetching definitions");
            throw new RuntimeException(e);
        }
    }


    private EntityLinearizationWrapperDto mapFromResponse(WhoficEntityLinearizationSpecification whoficSpecification, Date latsRevisionDate) {
        List<EntityLinearization> linearizations = whoficSpecification.linearizationSpecifications()
                .stream().map(this::mapFromSpecification).toList();
        if(whoficSpecification.linearizationResiduals() != null) {
            return new EntityLinearizationWrapperDto(whoficSpecification.linearizationResiduals().getSuppressOtherSpecifiedResiduals(),
                    whoficSpecification.linearizationResiduals().getSuppressUnspecifiedResiduals(),
                    latsRevisionDate,
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().getUnspecifiedResidualTitle()),
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().getOtherSpecifiedResidualTitle()),
                    linearizations);
        }
        return null;
    }

    private EntityLinearization mapFromSpecification(LinearizationSpecification specification) {
        return new EntityLinearization(specification.getIsAuxiliaryAxisChild() ,
                specification.getIsGrouping(),
                specification.getIsIncludedInLinearization(),
                specification.getLinearizationParent() == null ? "" : specification.getLinearizationParent().toString(),
                specification.getLinearizationView() == null ? "" : specification.getLinearizationView().toString(),
                specification.getCodingNote());
    }
}
