package edu.stanford.protege.gateway.linearization;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityLinearization;
import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.dto.LinearizationTitle;
import edu.stanford.protege.gateway.linearization.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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


    public List<LinearizationDefinition> getLinearizationDefinitions() {
        try {
            return definitionExecutor.execute(new LinearizationDefinitionRequest(), SecurityContextHelper.getExecutionContext()).get().definitionList();
        } catch (Exception e) {
            LOGGER.error("Error fetching linearization definitions");
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<EntityLinearizationWrapperDto> getEntityLinearizationDto(String entityIri, String projectId) {
        try {
            return entityLinearizationCommand.execute(new GetEntityLinearizationsRequest(entityIri, ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                    .thenApply(response -> mapFromResponse(response.linearizationSpecification(), response.lastRevisionDate()));
        } catch (Exception e) {
            LOGGER.error("Error fetching linearization of entity " + entityIri);
            throw new RuntimeException(e);
        }
    }



    public List<LinearizationDefinition> getDefinitionList(ExecutionContext executionContext) throws ExecutionException, InterruptedException {
        return definitionExecutor.execute(new LinearizationDefinitionRequest(), executionContext).thenApply(LinearizationDefinitionResponse::definitionList).get();
    }


    private EntityLinearizationWrapperDto mapFromResponse(WhoficEntityLinearizationSpecification whoficSpecification, Date latsRevisionDate) {
        List<EntityLinearization> linearizations = whoficSpecification.linearizationSpecifications()
                .stream().map(this::mapFromSpecification).toList();
        if (whoficSpecification.linearizationResiduals() != null) {
            return new EntityLinearizationWrapperDto(whoficSpecification.linearizationResiduals().getSuppressOtherSpecifiedResiduals(),
                    whoficSpecification.linearizationResiduals().getSuppressUnspecifiedResiduals(),
                    latsRevisionDate,
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().getUnspecifiedResidualTitle()),
                    new LinearizationTitle(whoficSpecification.linearizationResiduals().getOtherSpecifiedResidualTitle()),
                    linearizations);
        }
        return new EntityLinearizationWrapperDto(null,
                null,
                latsRevisionDate,
                null,
                null,
                linearizations);
    }

    private EntityLinearization mapFromSpecification(LinearizationSpecification specification) {
        return new EntityLinearization(specification.getIsAuxiliaryAxisChild(),
                specification.getIsGrouping(),
                specification.getIsIncludedInLinearization(),
                specification.getLinearizationParent() == null ? "" : specification.getLinearizationParent().toString(),
                specification.getLinearizationView() == null ? "" : specification.getLinearizationView().toString(),
                specification.getCodingNote());
    }
}
