package edu.stanford.protege.gateway.ontology.validators;

import edu.stanford.protege.gateway.dto.CreateEntityDto;
import edu.stanford.protege.gateway.ontology.OntologyService;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class CreateEntityValidatorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreateEntityValidatorService.class);

    private final OntologyService entityOntService;

    public CreateEntityValidatorService(OntologyService entityOntService) {
        this.entityOntService = entityOntService;
    }

    public void validateCreateEntityRequest(String projectId, CreateEntityDto createEntityDto) {
        validateTitle(createEntityDto.title());
        validateProjectId(projectId);
        validateEntityParents(projectId, createEntityDto.parent());
    }

    private void validateTitle(String title) {
        if(title == null || title.isBlank()){
            throw new IllegalArgumentException("Title title cannot be empty");
        }
        if (hasEscapeCharacters(title)) {
            throw new IllegalArgumentException(MessageFormat.format("Title has escape characters: $s. please remove any escape characters", title));
        }
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

    public static boolean hasEscapeCharacters(String input) {
        for (int i = 0; i < input.length() - 1; i++) {
            if (input.charAt(i) == '\\') {
                char nextChar = input.charAt(i + 1);
                if ("ntbrf\"'\\".indexOf(nextChar) != -1) {
                    return true;
                }
            }
        }
        return false;
    }
}
