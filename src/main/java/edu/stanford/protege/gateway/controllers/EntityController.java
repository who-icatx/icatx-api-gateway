package edu.stanford.protege.gateway.controllers;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.OwlEntityService;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.validators.CreateEntityValidatorService;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class EntityController {

    private final OwlEntityService owlEntityService;
    private final CreateEntityValidatorService createEntityValidator;

    public EntityController(OwlEntityService owlEntityService, CreateEntityValidatorService createEntityValidator) {
        this.owlEntityService = owlEntityService;
        this.createEntityValidator = createEntityValidator;
    }

    @GetMapping(value = "/{projectId}")
    public ResponseEntity<OWLEntityDto> getEntity(@PathVariable String projectId, @RequestParam String entityIri) {
        OWLEntityDto dto = owlEntityService.getEntityInfo(entityIri, projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        String etag = "";
        if (dto.lastChangeDate() != null) {
            httpHeaders.setLastModified(dto.lastChangeDate().toInstant());
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


    @PostMapping(value = "/{projectId}/createEntity")
    public ResponseEntity<List<OWLEntityDto>> createEntity(@PathVariable("projectId")
                                                           @NotNull(message = "Project ID cannot be null")
                                                           String projectId,
                                                           @RequestParam
                                                           @NotNull(message = "Entity name cannot be null")
                                                           @NotEmpty(message = "Entity name cannot be empty")
                                                           String entityName,
                                                           @RequestParam
                                                           @NotNull(message = "Entity parents cannot be null")
                                                           @NotEmpty(message = "Entity parents cannot be empty")
                                                           List<String> entityParents,
                                                           @RequestParam
                                                           @Nullable
                                                           String languageTag) {
        createEntityValidator.validateCreateEntityRequest(projectId, entityParents);
        var newCreatedIri = owlEntityService.createClassEntity(projectId, entityName, entityParents, languageTag);
        List<OWLEntityDto> result = newCreatedIri.stream()
                .map(newIri -> owlEntityService.getEntityInfo(newIri, projectId))
                .toList();
        return ResponseEntity.ok()
                .body(result);
    }
}
