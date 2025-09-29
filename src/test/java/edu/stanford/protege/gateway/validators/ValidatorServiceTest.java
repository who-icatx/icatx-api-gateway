package edu.stanford.protege.gateway.validators;

import edu.stanford.protege.gateway.EntityIsMissingException;
import edu.stanford.protege.gateway.dto.CreateEntityDto;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.semanticweb.owlapi.model.IRI;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ValidatorServiceTest {

    @Mock
    private CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;

    @Mock
    private CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;
    @Mock
    private CommandExecutor<GetExistingClassesForApiRequest, GetExistingClassesForApiResponse> getEntitySearchExecutor;

    private ValidatorService validatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validatorService = new ValidatorService(isExistingProjectExecutor, filterExistingEntitiesExecutor, getEntitySearchExecutor);
    }

    @Test
    void GIVEN_validInputs_WHEN_validateCreateEntityRequestCalled_THEN_noExceptionThrown() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        CreateEntityDto createEntityDto = new CreateEntityDto("Valid Title", "http://example.com/parent", null);
        IRI parentIri = IRI.create(createEntityDto.parent());

        when(isExistingProjectExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(GetIsExistingProjectResponse.create(true)));

        when(filterExistingEntitiesExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(FilterExistingEntitiesResponse.create(Set.of(parentIri))));

           assertDoesNotThrow(() -> validatorService.validateCreateEntityRequest(projectId, createEntityDto), "should not throw exception");
    }


    @Test
    void GIVEN_invalidProjectId_WHEN_validateCreateEntityRequestCalled_THEN_throwsIllegalArgumentException() {
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        CreateEntityDto createEntityDto = new CreateEntityDto("Valid Title", "http://example.com/parent", null);

        when(isExistingProjectExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(GetIsExistingProjectResponse.create(false)));

        assertThrows(IllegalArgumentException.class, () -> validatorService.validateCreateEntityRequest(projectId, createEntityDto));
    }

    @Test
    void GIVEN_invalidParent_WHEN_validateCreateEntityRequestCalled_THEN_throwsIllegalArgumentException() {
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        CreateEntityDto createEntityDto = new CreateEntityDto("Valid Title", "http://example.com/invalid-parent", null);

        when(isExistingProjectExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(GetIsExistingProjectResponse.create(true)));

        when(filterExistingEntitiesExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(FilterExistingEntitiesResponse.create(Collections.emptySet())));

        assertThrows(IllegalArgumentException.class, () -> validatorService.validateCreateEntityRequest(projectId, createEntityDto));
    }

    @Test
    void GIVEN_invalidTitle_WHEN_validateCreateEntityRequestCalled_THEN_throwsIllegalArgumentException() {
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        CreateEntityDto createEntityDto = new CreateEntityDto("Invalid\\nTitle", "http://example.com/parent", null);

        when(isExistingProjectExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(GetIsExistingProjectResponse.create(true)));

        when(filterExistingEntitiesExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(FilterExistingEntitiesResponse.create(Set.of(IRI.create(createEntityDto.parent())))));

        assertThrows(IllegalArgumentException.class, () -> validatorService.validateCreateEntityRequest(projectId, createEntityDto));
    }

    @Test
    void GIVEN_validInputs_WHEN_validateEntityExistsCalled_THEN_noExceptionThrown() throws Exception {
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entity = "http://example.com/entity";
        IRI entityIri = IRI.create(entity);

        when(filterExistingEntitiesExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(FilterExistingEntitiesResponse.create(Set.of(entityIri))));

        assertDoesNotThrow(() -> validatorService.validateEntityExists(projectId, entity));

    }

    @Test
    void GIVEN_nonexistentEntity_WHEN_validateEntityExistsCalled_THEN_throwsEntityIsMissingException() {
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entity = "http://example.com/nonexistent-entity";

        when(filterExistingEntitiesExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(FilterExistingEntitiesResponse.create(Collections.emptySet())));

        assertThrows(EntityIsMissingException.class, () -> validatorService.validateEntityExists(projectId, entity));
    }
}
