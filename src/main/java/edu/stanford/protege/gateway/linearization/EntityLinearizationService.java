package edu.stanford.protege.gateway.linearization;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.gateway.linearization.commands.*;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class EntityLinearizationService {

    private final Logger LOGGER = LoggerFactory.getLogger(EntityLinearizationService.class);

    private final CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> entityLinearizationCommand;

    private final CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> definitionExecutor;

    private final CommandExecutor<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> saveLinearizationCommand;

    public EntityLinearizationService(CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> entityLinearizationCommand, CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> definitionExecutor, CommandExecutor<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> saveLinearizationCommand) {
        this.entityLinearizationCommand = entityLinearizationCommand;
        this.definitionExecutor = definitionExecutor;
        this.saveLinearizationCommand = saveLinearizationCommand;
    }

    public CompletableFuture<EntityLinearizationWrapperDto> getEntityLinearizationDto(String entityIri, String projectId) {
        try {
            return entityLinearizationCommand.execute(new GetEntityLinearizationsRequest(entityIri, ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                    .thenApply(response -> LinearizationMapper.mapFromResponse(response.linearizationSpecification(), response.lastRevisionDate()));
        } catch (Exception e) {
            LOGGER.error("Error fetching linearization of entity " + entityIri);
            throw new RuntimeException(e);
        }
    }

    public List<LinearizationDefinition> getDefinitionList(ExecutionContext executionContext) throws ExecutionException, InterruptedException {
        return definitionExecutor.execute(new LinearizationDefinitionRequest(), executionContext)
                .thenApply(LinearizationDefinitionResponse::definitionList).get();
    }

    public void updateEntityLinearization(OWLEntityDto owlEntityDto, ProjectId projectId, ChangeRequestId changeRequestId) throws ExecutionException, InterruptedException {
        WhoficEntityLinearizationSpecification linearizationSpecification = LinearizationMapper.mapFromDto(owlEntityDto.entityIRI(), owlEntityDto.entityLinearizations());
        saveLinearizationCommand.execute(new SaveEntityLinearizationRequest(projectId, linearizationSpecification, changeRequestId), SecurityContextHelper.getExecutionContext()).get();
    }
}
