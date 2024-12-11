package edu.stanford.protege.gateway.controllers;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.OwlEntityService;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.validators.CreateEntityValidatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/projects")
@Validated
@Tag(name = "1. iCAT-X Entity Management", description = "APIs for managing iCAT-X entities")
public class ProjectsController {

    private final OwlEntityService owlEntityService;
    private final CreateEntityValidatorService createEntityValidator;

    public ProjectsController(OwlEntityService owlEntityService, CreateEntityValidatorService createEntityValidator) {
        this.owlEntityService = owlEntityService;
        this.createEntityValidator = createEntityValidator;
    }


    @GetMapping
    @Operation(summary = " Get projects", operationId = "1_getProjects")
    public ResponseEntity<List<ProjectSummaryDto>> getProjects() {
        var availableProjects = owlEntityService.getProjects();
        return ResponseEntity.ok()
                .body(availableProjects.stream().toList());
    }


    @GetMapping(value = "/{projectId}")
    @Operation(summary = "Reading an entity", operationId = "2_getEntity")
    public ResponseEntity<OWLEntityDto> getEntity(@PathVariable @javax.annotation.Nonnull String projectId, @RequestParam String entityIRI){
        OWLEntityDto dto = owlEntityService.getEntityInfo(entityIRI, projectId);
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

    @PutMapping(value = "/{projectId}/entities")
    @Operation(summary = "Updating an entity", operationId = "3_updateEntity")
    public ResponseEntity<OWLEntityDto> updateEntity( @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                      @PathVariable @Nonnull String projectId,
                                                      @RequestBody OWLEntityDto owlEntityDto){
        OWLEntityDto response = owlEntityService.updateEntity(owlEntityDto, projectId,ifMatch);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{projectId}/entities")
    @Operation(summary = "Adding a new entity", operationId = "4_createEntity")
    public ResponseEntity<OWLEntityDto> createEntity(@PathVariable("projectId")
                                                           @NotNull(message = "Project ID cannot be null")
                                                           String projectId,
                                                           @RequestBody CreateEntityDto createEntityDto) {
        createEntityValidator.validateCreateEntityRequest(projectId, createEntityDto);
        var newCreatedIri = owlEntityService.createClassEntity(projectId, createEntityDto);
        OWLEntityDto result = owlEntityService.getEntityInfo(newCreatedIri, projectId);
        return ResponseEntity.ok()
                .body(result);
    }

    @GetMapping(value = "/{projectId}/entities/children")
    @Operation(summary = "Get children for an entity", operationId = "5_getEntityChildren")
    public ResponseEntity<EntityChildren> getEntityChildren(@PathVariable String projectId, @RequestParam String entityIRI) {
        List<String> children = owlEntityService.getEntityChildren(entityIRI, projectId);

        return ResponseEntity.ok()
                .body(EntityChildren.create(children));
    }


    @GetMapping(value = "/{projectId}/entities/comments")
    @Operation(summary = "Comments for an entity", operationId = "6_getEntityComments")
    public ResponseEntity<EntityComments> getEntityComments(@PathVariable String projectId, @RequestParam String entityIRI) {
        EntityComments entityComments = owlEntityService.getEntityComments(entityIRI, projectId);

        return ResponseEntity.ok()
                .body(entityComments);
    }
}
