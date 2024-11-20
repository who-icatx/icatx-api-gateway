package edu.stanford.protege.gateway.ontology.validators;

import edu.stanford.protege.gateway.ontology.OntologyService;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class CreateEntityValidatorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreateEntityValidatorService.class);

    private final OntologyService entityOntService;

    public CreateEntityValidatorService(OntologyService entityOntService) {
        this.entityOntService = entityOntService;
    }

    public void validateCreateEntityRequest(String projectId, String parents) {
        validateProjectId(projectId);
        validateEntityParents(projectId, parents);
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

    private void validateEntityParents(String projectId, String parent) {
        if (parent == null || parent.isEmpty()) {
            throw new IllegalArgumentException("At least a parent should be specified!");
        }
        Set<String> existingParents;
        try {
            existingParents = entityOntService.getExistingEntities(projectId, parent).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parent:" + parent + " is valid!", e);
            throw new RuntimeException(e);
        }
        boolean isValid = existingParents.stream().anyMatch(existingParent -> existingParent.equals(parent));
        if (!isValid) {
            throw new IllegalArgumentException("Invalid Entity Parent: " + parent);
        }
    }
}
