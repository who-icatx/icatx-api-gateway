package edu.stanford.protege.gateway.postcoordination;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationWrapperDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValueResponse;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValuesRequest;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationRequest;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    public EntityPostCoordinationWrapperDto getEntityPostCoordination(String entityIri , String prjId) {
        ProjectId projectId = ProjectId.valueOf(prjId);
        ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();

        CompletableFuture<GetEntityCustomScaleValueResponse> future1 =
                customScaleExecutor.execute(new GetEntityCustomScaleValuesRequest(entityIri, projectId), executionContext);

        CompletableFuture<GetEntityPostCoordinationResponse> future2 =
                specificationExecutor.execute(new GetEntityPostCoordinationRequest(entityIri, projectId), executionContext);

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(future1, future2);
        completableFuture.join();


        try {
            GetEntityCustomScaleValueResponse customScaleValueResponse = future1.get();
            GetEntityPostCoordinationResponse  postCoordinationResponse = future2.get();

            return new EntityPostCoordinationWrapperDto(SpecificationMapper.mapFromResponse(postCoordinationResponse, linearizationService.getDefinitionList()),
                    getClosestToTodayDate(Arrays.asList(customScaleValueResponse.lastRevisionDate(), postCoordinationResponse.lastRevisionDate())),
                    CustomScalesMapper.mapFromResponse(customScaleValueResponse));

        } catch (Exception e) {
            LOGGER.error("Error fetching postcoordination data ", e);
            throw new RuntimeException("Error fetching the postcoordination", e);
        }
    }

}
