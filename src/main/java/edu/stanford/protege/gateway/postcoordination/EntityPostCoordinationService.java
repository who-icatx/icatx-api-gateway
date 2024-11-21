package edu.stanford.protege.gateway.postcoordination;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationWrapperDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class EntityPostCoordinationService {


    private final static Logger LOGGER = LoggerFactory.getLogger(EntityPostCoordinationService.class);


    private final CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor;
    private final CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor;

    private final CommandExecutor<AddEntityCustomScalesRevisionRequest, AddEntityCustomScalesRevisionResponse> updateCustomScalesExecutor;
    private final CommandExecutor<AddEntitySpecificationRevisionRequest, AddEntitySpecificationRevisionResponse> updateSpecificationExecutor;
    private final CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor;

    private final EntityLinearizationService linearizationService;

    public EntityPostCoordinationService(CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor,
                                         CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor, CommandExecutor<AddEntityCustomScalesRevisionRequest, AddEntityCustomScalesRevisionResponse> updateCustomScalesExecutor, CommandExecutor<AddEntitySpecificationRevisionRequest, AddEntitySpecificationRevisionResponse> updateSpecificationExecutor, CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor, EntityLinearizationService linearizationService) {
        this.customScaleExecutor = customScaleExecutor;
        this.specificationExecutor = specificationExecutor;
        this.updateCustomScalesExecutor = updateCustomScalesExecutor;
        this.updateSpecificationExecutor = updateSpecificationExecutor;
        this.tableConfigurationExecutor = tableConfigurationExecutor;
        this.linearizationService = linearizationService;
    }


    @Async
    public CompletableFuture<List<EntityPostCoordinationSpecificationDto>> getPostCoordinationSpecifications(String entityIri, String projectIdString) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
        return specificationExecutor.execute(new GetEntityPostCoordinationRequest(entityIri, projectId), executionContext)
                .thenApply(postCoordinationResponse -> {
                    try {
                        return SpecificationMapper.mapFromResponse(postCoordinationResponse, linearizationService.getDefinitionList(executionContext)
                        );
                    } catch (Exception e) {
                        LOGGER.error("Error fetching definition list ", e);
                        throw new RuntimeException(e);
                    }
                });
    }


    public void updateEntityPostCoordination(EntityPostCoordinationWrapperDto postcoordination, ProjectId projectId, String entityIri) {
        try {
            if(postcoordination != null) {
                ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
                WhoficCustomScalesValues customScalesValues = CustomScalesMapper.mapFromDtoList(entityIri, postcoordination.scaleCustomizations());
                TableConfiguration tableConfiguration = tableConfigurationExecutor.execute(new GetTablePostCoordinationAxisRequest("ICD"), executionContext).get().tableConfiguration();
                List<LinearizationDefinition> definitions = linearizationService.getDefinitionList(executionContext);
                WhoficEntityPostCoordinationSpecification specification = SpecificationMapper.mapFromDtoList(entityIri,
                        "ICD",
                        postcoordination.postcoordinationSpecifications(),
                        definitions,
                        tableConfiguration);

                updateCustomScalesExecutor.execute(new AddEntityCustomScalesRevisionRequest(projectId, customScalesValues), executionContext).get();
                updateSpecificationExecutor.execute(new AddEntitySpecificationRevisionRequest(projectId, specification), executionContext).get();
            }
        } catch (Exception e) {
            LOGGER.error("Error saving postcoordination for entity " + entityIri);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> getEntityCustomScales(String entityIri, String projectIdString) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
        return customScaleExecutor.execute(new GetEntityCustomScaleValuesRequest(entityIri, projectId), executionContext)
                .thenApply(CustomScalesMapper::mapFromResponse
                );
    }


}
