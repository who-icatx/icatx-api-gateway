package edu.stanford.protege.gateway.validators;

import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.gateway.EntityIsMissingException;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.BaseExclusionTerm;
import edu.stanford.protege.gateway.dto.CreateEntityDto;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.ontology.commands.FilterExistingEntitiesRequest;
import edu.stanford.protege.gateway.ontology.commands.FilterExistingEntitiesResponse;
import edu.stanford.protege.gateway.ontology.commands.GetIsExistingProjectRequest;
import edu.stanford.protege.gateway.ontology.commands.GetIsExistingProjectResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ValidatorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);

    private final CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;
    private final CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;


    public ValidatorService(CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor, CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor) {
        this.isExistingProjectExecutor = isExistingProjectExecutor;
        this.filterExistingEntitiesExecutor = filterExistingEntitiesExecutor;
    }


    public void validateCreateEntityRequest(String projectId, CreateEntityDto createEntityDto) {
        validateTitle(createEntityDto.title());
        validateProjectId(projectId);
        validateEntityParents(projectId, createEntityDto.parent());
    }

    @Async
    private CompletableFuture<Boolean> isExistingProject(String projectId) {
        return isExistingProjectExecutor.execute(GetIsExistingProjectRequest.create(ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(GetIsExistingProjectResponse::isExistingProject);
    }

    @Async
    private CompletableFuture<Set<String>> getExistingEntities(String projectId, String entity) {
        var entityIri = IRI.create(entity);
        return filterExistingEntitiesExecutor.execute(FilterExistingEntitiesRequest.create(ProjectId.valueOf(projectId), ImmutableSet.of(entityIri)), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.existingEntities()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toSet())
                );
    }

    private void validateTitle(String title) {
        if(title == null || title.isBlank()){
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (hasEscapeCharacters(title)) {
            throw new IllegalArgumentException(MessageFormat.format("Title has escape characters: {0}. please remove any escape characters", title));
        }
    }

    public void validateProjectId(String projectId) {
        boolean projectExists;
        try {
            projectExists = this.isExistingProject(projectId).get();
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
            existingParents = this.getExistingEntities(projectId, parent).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parent:" + parent + " is valid!", e);
            throw new RuntimeException(e);
        }
        boolean isValid = existingParents.stream().anyMatch(existingParent -> existingParent.equals(parent));
        if (!isValid) {
            throw new IllegalArgumentException("Invalid Entity Parent: " + parent);
        }
    }

    public void validateEntityExists(String projectId, String entity){
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("At least an entityUri should be specified!");
        }
        Set<String> existingEntities;
        try {
            existingEntities = this.getExistingEntities(projectId, entity).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not verify if parent:" + entity + " is valid!", e);
            throw new RuntimeException(e);
        }
        boolean isValid = existingEntities.stream().anyMatch(existingParent -> existingParent.equals(entity));
        if (!isValid) {
            throw new EntityIsMissingException("Invalid Entity IRI: " + entity);
        }
    }

    public void validateBaseExclusionTerms(List<BaseExclusionTerm> baseExclusionTerms) {
        if (baseExclusionTerms != null) {
            for (BaseExclusionTerm term : baseExclusionTerms) {
                validateBaseExclusionTerm(term);
            }
        }
    }

    private void validateBaseExclusionTerm(BaseExclusionTerm term) {
        if (term == null) {
            throw new IllegalArgumentException("BaseExclusionTerm cannot be null");
        }
        
        if (term.foundationReference() == null || term.foundationReference().trim().isEmpty()) {
            throw new IllegalArgumentException("BaseExclusionTerm has invalid foundationReference: cannot be null, empty, or blank");
        }
    }

    public void validateOWLEntityDto(OWLEntityDto owlEntityDto) {
        if (owlEntityDto == null) {
            throw new IllegalArgumentException("OWLEntityDto cannot be null");
        }
        
        if (owlEntityDto.languageTerms() != null && owlEntityDto.languageTerms().baseExclusionTerms() != null) {
            validateBaseExclusionTerms(owlEntityDto.languageTerms().baseExclusionTerms());
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
