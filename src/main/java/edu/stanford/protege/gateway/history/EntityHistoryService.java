package edu.stanford.protege.gateway.history;

import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class EntityHistoryService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityHistoryService.class);

    private final CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor;
    private final CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> entityHistorySummaryExecutor;

    public EntityHistoryService(CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor,
                                CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> entityHistorySummaryExecutor) {
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.entityHistorySummaryExecutor = entityHistorySummaryExecutor;
    }

    public ChangedEntities getChangedEntities(String projectId, Timestamp timestamp) {

        try {
            return changedEntitiesExecutor.execute(GetChangedEntitiesRequest.create(ProjectId.valueOf(projectId), timestamp), SecurityContextHelper.getExecutionContext())
                    .thenApply(GetChangedEntitiesResponse::changedEntities)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while requestion changed entities." + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public EntityHistorySummary getEntityHistorySummary(String projectId, String entityIri) {
        try {
            return entityHistorySummaryExecutor.execute(GetEntityHistorySummaryRequest.create(projectId, entityIri), SecurityContextHelper.getExecutionContext())
                    .thenApply(GetEntityHistorySummaryResponse::entityHistorySummary)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while requestion entity history summary. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public CompletableFuture<LocalDateTime> getEntityLatestChangeTime(String projectId, String entityIri){
        return getEntityLatestChangeTime(projectId, entityIri, SecurityContextHelper.getExecutionContext());
    }


    @Async
    public CompletableFuture<LocalDateTime> getEntityLatestChangeTime(String projectId, String entityIri, ExecutionContext executionContext) {
        return entityHistorySummaryExecutor.execute(new GetEntityHistorySummaryRequest(projectId, entityIri), executionContext)
                .thenApply(response -> {
                    if (response.entityHistorySummary() != null && response.entityHistorySummary().changes() != null && response.entityHistorySummary().changes().size() > 0) {
                        response.entityHistorySummary().changes().sort(Comparator.comparing(EntityChange::timestamp).reversed());
                        return response.entityHistorySummary().changes().get(0).timestamp();
                    }
                    return LocalDateTime.MIN;
                });
    }
}
