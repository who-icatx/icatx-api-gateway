package edu.stanford.protege.gateway.controllers;


import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.OwlEntityService;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.maintenance.ProjectMaintenanceState;
import edu.stanford.protege.gateway.maintenance.ProjectUnderMaintenanceException;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@Validated
@Tag(name = "1. iCAT-X Entity Management", description = "APIs for managing iCAT-X entities")
public class ProjectsController {

    private final OwlEntityService owlEntityService;
    private final ProjectMaintenanceState projectMaintenanceState;

    public ProjectsController(OwlEntityService owlEntityService, ProjectMaintenanceState projectMaintenanceState) {
        this.owlEntityService = owlEntityService;
        this.projectMaintenanceState = projectMaintenanceState;
    }


    @GetMapping
    @Operation(summary = " Get projects", operationId = "1_getProjects")
    public ResponseEntity<List<ProjectSummaryDto>> getProjects() {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        var availableProjects = owlEntityService.getProjects();
        return ResponseEntity.ok()
                .body(availableProjects.stream().toList());
    }


    @GetMapping(value = "/{projectId}")
    @Operation(summary = "Reading an entity", operationId = "2_getEntity")
    public ResponseEntity<OWLEntityDto> getEntity(@PathVariable @javax.annotation.Nonnull String projectId, @RequestParam String entityIRI) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        checkMaintenanceState(ProjectId.valueOf(projectId));
        OWLEntityDto dto = owlEntityService.getEntityInfo(entityIRI, projectId);
        return getOwlEntityDtoResponseEntity(dto);
    }

    @PutMapping(value = "/{projectId}/entities")
    @Operation(summary = "Updating an entity", operationId = "3_updateEntity")
    public ResponseEntity<OWLEntityDto> updateEntity(@RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                     @PathVariable @Nonnull String projectId,
                                                     @RequestBody @Valid OWLEntityDto owlEntityDto) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        checkMaintenanceState(ProjectId.valueOf(projectId));

        OWLEntityDto response = owlEntityService.updateEntity(owlEntityDto, projectId, ifMatch);
        return getOwlEntityDtoResponseEntity(response);
    }

    @PostMapping(value = "/{projectId}/entities")
    @Operation(summary = "Adding a new entity", operationId = "4_createEntity")
    public ResponseEntity<OWLEntityDto> createEntity(@PathVariable("projectId")
                                                     @NotNull(message = "Project ID cannot be null")
                                                     String projectId,
                                                     @RequestBody CreateEntityDto createEntityDto) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        checkMaintenanceState(ProjectId.valueOf(projectId));

        var newCreatedIri = owlEntityService.createClassEntity(projectId, createEntityDto);
        OWLEntityDto result = owlEntityService.getEntityInfo(newCreatedIri, projectId);
        return getOwlEntityDtoResponseEntity(result);

    }

    @GetMapping(value = "/{projectId}/entities/children")
    @Operation(summary = "Get children for an entity", operationId = "5_getEntityChildren")
    public ResponseEntity<EntityChildren> getEntityChildren(@PathVariable String projectId, @RequestParam String entityIRI) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        checkMaintenanceState(ProjectId.valueOf(projectId));

        List<String> children = owlEntityService.getEntityChildren(entityIRI, projectId);

        return ResponseEntity.ok()
                .body(EntityChildren.create(projectId, entityIRI, children));
    }


    @GetMapping(value = "/{projectId}/entities/comments")
    @Operation(summary = "Comments for an entity", operationId = "6_getEntityComments")
    public ResponseEntity<EntityComments> getEntityComments(@PathVariable String projectId, @RequestParam String entityIRI) {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());
        checkMaintenanceState(ProjectId.valueOf(projectId));

        EntityComments entityComments = owlEntityService.getEntityComments(entityIRI, projectId);

        return ResponseEntity.ok()
                .body(entityComments);
    }

    private static ResponseEntity<OWLEntityDto> getOwlEntityDtoResponseEntity(OWLEntityDto dto) {
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

    private void checkMaintenanceState(ProjectId projectId) {
        if(this.projectMaintenanceState.isUnderMaintenance(projectId, SecurityContextHelper.getExecutionContext())) {
            throw new ProjectUnderMaintenanceException(projectId.id());
        }
    }
}
