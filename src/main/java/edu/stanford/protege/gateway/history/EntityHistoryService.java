package edu.stanford.protege.gateway.history;

import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.ChangedEntities;
import edu.stanford.protege.gateway.history.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.slf4j.*;
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
    private final CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> historySummaryExecutor;

    public EntityHistoryService(CommandExecutor<GetChangedEntitiesRequest, GetChangedEntitiesResponse> changedEntitiesExecutor, CommandExecutor<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> historySummaryExecutor) {
        this.changedEntitiesExecutor = changedEntitiesExecutor;
        this.historySummaryExecutor = historySummaryExecutor;
    }

    public ChangedEntities getChangedEntities(String projectId, Timestamp timestamp) {

        try {
            return changedEntitiesExecutor.execute(GetChangedEntitiesRequest.create(ProjectId.valueOf(projectId), timestamp), SecurityContextHelper.getExecutionContext())
                    .thenApply(GetChangedEntitiesResponse::changedEntities)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while requestion changed entities");
            throw new RuntimeException(e);
        }

    }

    public CompletableFuture<LocalDateTime> getEntityLatestChangeTime(String projectId, String entityIri) {
        return historySummaryExecutor.execute(new GetEntityHistorySummaryRequest(projectId, entityIri), SecurityContextHelper.getExecutionContext())
                .thenApply(response -> {
                    if (response.entityHistorySummary() != null && response.entityHistorySummary().changes() != null && response.entityHistorySummary().changes().size() > 0) {
                        response.entityHistorySummary().changes().sort(Comparator.comparing(EntityChange::dateTime).reversed());
                        return response.entityHistorySummary().changes().get(0).dateTime();
                    }
                    return LocalDateTime.MIN;
                });
    }
}
