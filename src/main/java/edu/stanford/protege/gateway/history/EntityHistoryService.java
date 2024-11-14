package edu.stanford.protege.gateway.history;

import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.commands.*;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
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
            LOGGER.error("Error while requestion changed entities");
            throw new RuntimeException(e);
        }

    }

    public EntityHistory getEntityHistory(String projectId, String entityIri) {
        EntityHistory entityHistory = entityHistorySummaryExecutor.execute(GetEntityHistorySummaryRequest.create(projectId,entityIri), SecurityContextHelper.getExecutionContext())
                .thenApply(response -> mapToEntityHistory(response.projectChangesForEntity()))
                .get();
        return null;
    }

    private Object mapToEntityHistory(List<ProjectChange> projectChangesForEntity) {
    }
}
