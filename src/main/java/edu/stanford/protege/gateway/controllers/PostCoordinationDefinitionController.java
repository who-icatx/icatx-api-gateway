package edu.stanford.protege.gateway.controllers;

import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.gateway.postcoordination.commands.CompletePostCoordinationAxisConfiguration;
import edu.stanford.protege.webprotege.ipc.util.CorrelationMDCUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/postcoordination-definitions")
@Tag(name = "4. iCAT-X PostCoordination Definitions", description = "APIs for querying iCAT-X postcoordination definitions")
public class PostCoordinationDefinitionController {


    private final EntityPostCoordinationService entityPostCoordinationService;

    public PostCoordinationDefinitionController(EntityPostCoordinationService entityPostCoordinationService) {
        this.entityPostCoordinationService = entityPostCoordinationService;
    }


    @GetMapping
    @Operation(summary = "Get complete postcoordination definitions", operationId = "10_getPostCoordinationDefinitions")
    public ResponseEntity<List<CompletePostCoordinationAxisConfiguration>> getLinearizationDefinitions() throws ExecutionException, InterruptedException, TimeoutException {
        CorrelationMDCUtil.setCorrelationId(UUID.randomUUID().toString());

        return ResponseEntity.ok(entityPostCoordinationService.getAllPostCoordinationAxisConfigs());
    }

}
