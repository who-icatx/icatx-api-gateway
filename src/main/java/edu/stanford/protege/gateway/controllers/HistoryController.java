package edu.stanford.protege.gateway.controllers;


import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.*;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final EntityHistoryService entityHistoryService;

    public HistoryController(EntityHistoryService entityHistoryService) {
        this.entityHistoryService = entityHistoryService;
    }

    @GetMapping("/changedEntities")
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

    @GetMapping("/entityHistorySummary")
    public ResponseEntity<EntityHistorySummary> getEntityHistorySummary(@RequestParam("projectId") String projectId,
                                                                        @RequestParam("entityIri") String entityIri) {
        EntityHistorySummary entityHistorySummary = entityHistoryService.getEntityHistorySummary(projectId, entityIri);
        return ResponseEntity.ok()
                .body(entityHistorySummary);

    }
}
