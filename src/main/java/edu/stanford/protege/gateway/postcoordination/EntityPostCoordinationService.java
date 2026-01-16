package edu.stanford.protege.gateway.postcoordination;


import edu.stanford.protege.gateway.ApplicationException;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationWrapperDto;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.*;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class EntityPostCoordinationService {


    private final static Logger LOGGER = LoggerFactory.getLogger(EntityPostCoordinationService.class);


    private final CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor;
    private final CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor;

    private final CommandExecutor<AddEntityCustomScalesRevisionRequest, AddEntityCustomScalesRevisionResponse> updateCustomScalesExecutor;
    private final CommandExecutor<AddEntitySpecificationRevisionRequest, AddEntitySpecificationRevisionResponse> updateSpecificationExecutor;
    private final CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor;
    private final CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> entityTypesExecutor;

    private final CommandExecutor<GetFullPostCoordinationConfigurationRequest, GetFullPostCoordinationConfigurationResponse> entityConfigurationExecutor;

    private final EntityLinearizationService linearizationService;

    public EntityPostCoordinationService(CommandExecutor<GetEntityCustomScaleValuesRequest, GetEntityCustomScaleValueResponse> customScaleExecutor,
                                         CommandExecutor<GetEntityPostCoordinationRequest, GetEntityPostCoordinationResponse> specificationExecutor,
                                         CommandExecutor<AddEntityCustomScalesRevisionRequest, AddEntityCustomScalesRevisionResponse> updateCustomScalesExecutor,
                                         CommandExecutor<AddEntitySpecificationRevisionRequest, AddEntitySpecificationRevisionResponse> updateSpecificationExecutor,
                                         CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor,
                                         CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> entityTypesExecutor, CommandExecutor<GetFullPostCoordinationConfigurationRequest, GetFullPostCoordinationConfigurationResponse> entityConfigurationExecutor,
                                         EntityLinearizationService linearizationService) {
        this.customScaleExecutor = customScaleExecutor;
        this.specificationExecutor = specificationExecutor;
        this.updateCustomScalesExecutor = updateCustomScalesExecutor;
        this.updateSpecificationExecutor = updateSpecificationExecutor;
        this.tableConfigurationExecutor = tableConfigurationExecutor;
        this.entityTypesExecutor = entityTypesExecutor;
        this.entityConfigurationExecutor = entityConfigurationExecutor;
        this.linearizationService = linearizationService;
    }

    @Async
    public CompletableFuture<List<EntityPostCoordinationSpecificationDto>> getPostCoordinationSpecifications(String entityIri, String projectIdString){
        return getPostCoordinationSpecifications(entityIri, projectIdString, SecurityContextHelper.getExecutionContext());
    }
    @Async
    public CompletableFuture<List<EntityPostCoordinationSpecificationDto>> getPostCoordinationSpecifications(String entityIri, String projectIdString, ExecutionContext executionContext) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
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



    public void updateEntityPostCoordination(EntityPostCoordinationWrapperDto postcoordination, ProjectId projectId, String entityIri, ChangeRequestId changeRequestId) {
        try {
            if(postcoordination != null) {
                ExecutionContext executionContext = SecurityContextHelper.getExecutionContext();
                WhoficCustomScalesValues customScalesValues = CustomScalesMapper.mapFromDtoList(entityIri, postcoordination.scaleCustomizations());
                TableConfiguration tableConfiguration = tableConfigurationExecutor.execute(new GetTablePostCoordinationAxisRequest(IRI.create(entityIri), projectId), executionContext).get().tableConfiguration();
                List<String> entityTypes = entityTypesExecutor.execute(GetIcatxEntityTypeRequest.create(IRI.create(entityIri), projectId), executionContext).get().icatxEntityTypes();
                List<LinearizationDefinition> definitions = linearizationService.getDefinitionList(executionContext);
                WhoficEntityPostCoordinationSpecification specification = SpecificationMapper.mapFromDtoList(entityIri,
                        entityTypes.isEmpty()?"ICD":entityTypes.get(0),
                        postcoordination.postcoordinationSpecifications(),
                        definitions,
                        tableConfiguration);

                updateCustomScalesExecutor.execute(new AddEntityCustomScalesRevisionRequest(projectId, customScalesValues, changeRequestId), executionContext).get();
                updateSpecificationExecutor.execute(new AddEntitySpecificationRevisionRequest(projectId, specification, changeRequestId), executionContext).get();
            }
        } catch (Exception e) {
            LOGGER.error("Error saving postcoordination for entity " + entityIri, e);
            throw new ApplicationException("Error saving postcoordination for entity " + entityIri);
        }
    }

    @Async
    public CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> getEntityCustomScales(String entityIri, String projectIdString){
        return getEntityCustomScales(entityIri, projectIdString, SecurityContextHelper.getExecutionContext());
    }

    @Async
    public CompletableFuture<List<EntityPostCoordinationCustomScalesDto>> getEntityCustomScales(String entityIri, String projectIdString, ExecutionContext executionContext) {
        ProjectId projectId = ProjectId.valueOf(projectIdString);
        return customScaleExecutor.execute(new GetEntityCustomScaleValuesRequest(entityIri, projectId), executionContext)
                .thenApply(CustomScalesMapper::mapFromResponse
                );
    }

    public List<CompletePostCoordinationAxisConfiguration> getAllPostCoordinationAxisConfigs() throws ExecutionException, InterruptedException, TimeoutException {
        return entityConfigurationExecutor.execute(new GetFullPostCoordinationConfigurationRequest(), SecurityContextHelper.getExecutionContext()).get(5, TimeUnit.SECONDS).configuration();
    }
}
