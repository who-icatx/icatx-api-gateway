package edu.stanford.protege.gateway.ontology.validators;

import edu.stanford.protege.gateway.ontology.OntologyService;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CreateEntityValidatorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreateEntityValidatorService.class);

    private final OntologyService entityOntService;

    public CreateEntityValidatorService(OntologyService entityOntService) {
        this.entityOntService = entityOntService;
    }

    public void validateCreateEntityRequest(String projectId, List<String> entityParents) {
        validateProjectId(projectId);
        validateEntityParents(projectId, entityParents);
    }

    private void validateProjectId(String projectId) {
        boolean projectExists;
        try {
            projectExists = entityOntService.isExistingProject(projectId).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if projectId:" + projectId + " is valid!", e);
            throw new RuntimeException(e);
        }
        if (!projectExists) {
            throw new IllegalArgumentException("Invalid Project ID: " + projectId);
        }
    }

    private void validateEntityParents(String projectId, List<String> entityParents) {
        if(entityParents == null || entityParents.isEmpty()){
            throw new IllegalArgumentException("At least a parent should be specified!");
        }
        Set<String> existingParents;
        try {
            existingParents = entityOntService.getExistingEntities(projectId, entityParents).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parentEntities:" + entityParents + " are valid!", e);
            throw new RuntimeException(e);
        }
        var invalidParents = entityParents.stream()
                .filter(parent -> existingParents.stream().noneMatch(existingParent -> existingParent.equals(parent))).toList();
        if (!invalidParents.isEmpty()) {
            throw new IllegalArgumentException("Invalid Entity Parents: " + entityParents);
        }
    }
}
