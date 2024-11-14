package edu.stanford.protege.gateway.postcoordination;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationWrapperDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
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
import java.util.stream.Collectors;

import static edu.stanford.protege.gateway.MappingHelper.getClosestToTodayDate;

@Service
public class EntityPostCoordinationService {


    private final static Logger LOGGER = LoggerFactory.getLogger(EntityPostCoordinationService.class);


    private final CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor;
    private final CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor;

    private final EntityLinearizationService linearizationService;

    public EntityPostCoordinationService(CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor,
                                         CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor, EntityLinearizationService linearizationService) {
        this.customScaleExecutor = customScaleExecutor;
        this.specificationExecutor = specificationExecutor;
        this.linearizationService = linearizationService;
    }


    @Async
    public CompletableFuture<List<EntityPostCoordinationSpecificationDto>> getPostCoordinationSpecifications(String entityIri, String projectIdString) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
        return  specificationExecutor.execute(new GetEntityPostCoordinationRequest(entityIri, projectId), executionContext)
                .thenApply(postCoordinationResponse -> {
                    try {
                        return SpecificationMapper.mapFromResponse(postCoordinationResponse, linearizationService.getDefinitionList(executionContext)
                        );
                    } catch (Exception e) {
                        LOGGER.error("Error fetching definition list " , e);
                        throw new RuntimeException(e);
                    }
                });
    }

    public CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> getEntityCustomScales(String entityIri, String projectIdString) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
        return  customScaleExecutor.execute(new GetEntityCustomScaleValuesRequest(entityIri, projectId), executionContext)
                .thenApply(CustomScalesMapper::mapFromResponse
                );
    }


}
