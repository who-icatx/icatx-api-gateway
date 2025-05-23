package edu.stanford.protege.gateway.controllers;


import edu.stanford.protege.gateway.dto.ChangedEntities;
import edu.stanford.protege.gateway.dto.EntityHistorySummary;
import edu.stanford.protege.gateway.dto.EntityHistorySummaryDto;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/history")
@Tag(name = "2. iCAT-X Entity History Query", description = "APIs for querying iCAT-X entity history")
public class HistoryController {

    private final EntityHistoryService entityHistoryService;

    public HistoryController(EntityHistoryService entityHistoryService) {
        this.entityHistoryService = entityHistoryService;
    }

    @GetMapping("/entities")
    @Operation(summary = "Entities that have been updated since a certain time", operationId = "7_getChangedEntities")
    public ResponseEntity<ChangedEntities> getChangedEntities(@RequestParam("projectId") String projectId,
                                                              @RequestParam("changedAfter")
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                              ZonedDateTime changedAfter) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        LocalDateTime utcDateTime = changedAfter.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(utcDateTime);
        ChangedEntities changedEntities = entityHistoryService.getChangedEntities(projectId, timestamp);
        return ResponseEntity.ok()
                .body(changedEntities);

    }

    @GetMapping("/entity-summary")
    @Operation(summary = "Change history for a single entity", operationId = "8_getEntityHistory")
    public ResponseEntity<EntityHistorySummaryDto> getEntityHistorySummary(@RequestParam("projectId") String projectId,
                                                                           @RequestParam("entityIRI") String entityIRI) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        EntityHistorySummary entityHistorySummary = entityHistoryService.getEntityHistorySummary(projectId, entityIRI);
        return ResponseEntity.ok()
                .body(EntityHistorySummaryDto.create(
                                entityHistorySummary.projectId(),
                                entityHistorySummary.entityUri(),
                                entityHistorySummary.changes()
                        )
                );

    }
}
