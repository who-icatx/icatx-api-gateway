package edu.stanford.protege.gateway.controllers;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/linearization-definitions")
public class LinearizationDefinitionController {


    private final EntityLinearizationService entityLinearizationService;

    public LinearizationDefinitionController(EntityLinearizationService entityLinearizationService) {
        this.entityLinearizationService = entityLinearizationService;
    }


    @GetMapping
    public ResponseEntity<List<LinearizationDefinition>> getLinearizationDefinitions() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(entityLinearizationService.getDefinitionList(SecurityContextHelper.getExecutionContext()));
    }


}
