package edu.stanford.protege.gateway.controllers;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.OwlEntityService;
import edu.stanford.protege.gateway.dto.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class EntityController {

    private final OwlEntityService owlEntityService;

    public EntityController(OwlEntityService owlEntityService) {
        this.owlEntityService = owlEntityService;
    }

    @GetMapping(value = "/{projectId}")
    public ResponseEntity<OWLEntityDto> getEntity(@PathVariable @Nonnull String projectId, @RequestParam String entityIri){
        OWLEntityDto dto = owlEntityService.getEntityInfo(entityIri, projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        String etag = "";
        if (dto.lastChangeDate() != null) {
            httpHeaders.setLastModified(dto.lastChangeDate().toInstant(ZoneOffset.UTC));
            etag = Hashing.sha256().hashString(dto.lastChangeDate().toString(), StandardCharsets.UTF_8).toString();
        }
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .eTag(etag)
                .body(dto);
    }

    @GetMapping(value = "/{projectId}/entityChildren")
    public ResponseEntity<EntityChildren> getEntityChildren(@PathVariable String projectId, @RequestParam String entityIri) {
        List<String> children = owlEntityService.getEntityChildren(entityIri, projectId);

        return ResponseEntity.ok()
                .body(EntityChildren.create(children));
    }

    @PutMapping(value = "/{projectId}/entities")
    public ResponseEntity<OWLEntityDto> updateEntity( @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                     @PathVariable @Nonnull String projectId,
                                                     @RequestBody OWLEntityDto owlEntityDto){
        OWLEntityDto response = owlEntityService.updateEntity(owlEntityDto, projectId,ifMatch);
        return ResponseEntity.ok(response);
    }
}
