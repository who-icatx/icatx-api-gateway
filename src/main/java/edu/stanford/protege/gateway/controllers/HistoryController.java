package edu.stanford.protege.gateway.controllers;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.*;

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
        LocalDateTime utcDateTime = changedAfter.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(utcDateTime);
        ChangedEntities changedEntities = entityHistoryService.getChangedEntities(projectId, timestamp);
        return ResponseEntity.ok()
                .body(changedEntities);

    }

    @GetMapping("/entity-summary")
    @Operation(summary = "Change history for a single entity", operationId = "8_getEntityHistory")
    public ResponseEntity<EntityHistorySummary> getEntityHistorySummary(@RequestParam("projectId") String projectId,
                                                                        @RequestParam("entityIri") String entityIri) {
        EntityHistorySummary entityHistorySummary = entityHistoryService.getEntityHistorySummary(projectId, entityIri);
        return ResponseEntity.ok()
                .body(entityHistorySummary);

    }
}
