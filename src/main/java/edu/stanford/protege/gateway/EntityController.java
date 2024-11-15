package edu.stanford.protege.gateway;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

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
        if(dto.lastChangeDate() != null) {
            httpHeaders.setLastModified(dto.lastChangeDate().toInstant());
            etag = Hashing.sha256().hashString(dto.lastChangeDate().toString(), StandardCharsets.UTF_8).toString();
        }
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .eTag(etag)
                .body(dto);
    }

    @PutMapping(value = "/{projectId}/entities")
    public ResponseEntity<OWLEntityDto> updateEntity(@PathVariable @Nonnull String projectId, @RequestBody OWLEntityDto owlEntityDto){
        OWLEntityDto response = owlEntityService.updateEntity(owlEntityDto, projectId);
        return ResponseEntity.ok(response);
    }
}
