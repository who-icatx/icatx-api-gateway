package edu.stanford.protege.gateway.history;


import edu.stanford.protege.gateway.dto.ChangedEntities;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final EntityHistoryService entityHistoryService;

    public HistoryController(EntityHistoryService entityHistoryService) {
        this.entityHistoryService = entityHistoryService;
    }

    @GetMapping("/entityChanges")
    public ResponseEntity<ChangedEntities> getEntityChanges(@RequestParam("projectId") String projectId,
                                                     @RequestParam("changedAfter")
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     LocalDateTime changedAfter) {
        var timestamp = Timestamp.valueOf(changedAfter);
        ChangedEntities changedEntities = entityHistoryService.getChangedEntities(projectId, timestamp);
        return ResponseEntity.ok()
                .body(changedEntities);


    }
}
