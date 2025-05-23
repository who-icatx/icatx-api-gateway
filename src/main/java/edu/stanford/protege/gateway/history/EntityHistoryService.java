package edu.stanford.protege.gateway.history;

import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.ChangedEntities;
import edu.stanford.protege.gateway.dto.EntityChange;
import edu.stanford.protege.gateway.dto.EntityHistorySummary;
import edu.stanford.protege.gateway.history.commands.GetChangedEntitiesRequest;
import edu.stanford.protege.gateway.history.commands.GetChangedEntitiesResponse;
import edu.stanford.protege.gateway.history.commands.GetEntityHistorySummaryRequest;
import edu.stanford.protege.gateway.history.commands.GetEntityHistorySummaryResponse;
import edu.stanford.protege.gateway.ontology.commands.GetAvailableProjectsForApiRequest;
import edu.stanford.protege.gateway.ontology.commands.GetAvailableProjectsForApiResponse;
import edu.stanford.protege.gateway.validators.ValidatorService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class EntityHistoryService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityHistoryService.class);

    private final ValidatorService validatorService;

    private final CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor;
    private final CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> entityHistorySummaryExecutor;
    private final CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor;

    public EntityHistoryService(ValidatorService validatorService,
                                CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor,
                                CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> entityHistorySummaryExecutor,
                                CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getProjectsExecutor) {
        this.validatorService = validatorService;
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.entityHistorySummaryExecutor = entityHistorySummaryExecutor;
        this.getProjectsExecutor = getProjectsExecutor;
    }

    public ChangedEntities getChangedEntities(String projectId, Timestamp timestamp) {
        validatorService.validateProjectId(projectId);

        try {
            return changedEntitiesExecutor.execute(GetChangedEntitiesRequest.create(ProjectId.valueOf(projectId), timestamp.getTime()), SecurityContextHelper.getExecutionContext())
                    .thenApply(GetChangedEntitiesResponse::changedEntities)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while requestion changed entities." + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public EntityHistorySummary getEntityHistorySummary(String projectId, String entityIri) {
        validatorService.validateProjectId(projectId);
        validatorService.validateEntityExists(projectId, entityIri);
        try {
            return entityHistorySummaryExecutor.execute(GetEntityHistorySummaryRequest.create(projectId, entityIri), SecurityContextHelper.getExecutionContext())
                    .thenApply(GetEntityHistorySummaryResponse::entityHistorySummary)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while requestion entity history summary. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<LocalDateTime> getEntityLatestChangeTime(String projectId, String entityIri) {
        return getEntityLatestChangeTime(projectId, entityIri, SecurityContextHelper.getExecutionContext());
    }


    @Async
    public CompletableFuture<LocalDateTime> getEntityLatestChangeTime(String projectId, String entityIri, ExecutionContext executionContext) {
        return entityHistorySummaryExecutor.execute(new GetEntityHistorySummaryRequest(projectId, entityIri), executionContext)
                .thenApply(response -> {
                    if (response.entityHistorySummary() != null && response.entityHistorySummary().changes() != null && !response.entityHistorySummary().changes().isEmpty()) {
                        var sortedHistory = response.entityHistorySummary().changes()
                                .stream()
                                .filter(entityChange -> entityChange.timestamp() != null)
                                .sorted(Comparator.comparing(EntityChange::timestamp).reversed())
                                .toList();
                        if(!sortedHistory.isEmpty()){
                            return sortedHistory.get(0).timestamp();
                        }
                    }

                    try {
                        return getProjectsExecutor.execute(GetAvailableProjectsForApiRequest.create(), executionContext)
                                .get().availableProjects().stream()
                                .filter(p -> p.projectId().equals(projectId))
                                .findFirst()
                                .map(p -> LocalDateTime.ofInstant(Instant.ofEpochMilli(p.createdAt()), ZoneId.systemDefault()))
                                .orElseThrow(() -> new RuntimeException("Couldn't find existing project " + projectId));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
