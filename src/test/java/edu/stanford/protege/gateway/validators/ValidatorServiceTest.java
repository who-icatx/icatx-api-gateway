package edu.stanford.protege.gateway.validators;

import edu.stanford.protege.gateway.EntityIsMissingException;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.postcoordination.commands.*;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.semanticweb.owlapi.model.IRI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    @Mock
    CommandExecutor<ValidateEntityUpdateRequest, ValidateEntityUpdateResponse> validateEntityUpdateExecutor;
    @Mock
    CommandExecutor<GetTablePostCoordinationAxisRequest, GetTablePostCoordinationAxisResponse> tableConfigurationExecutor;
    @Mock
    CommandExecutor<GetIcatxEntityTypeRequest, GetIcatxEntityTypeResponse> entityTypesExecutor;
    @Mock
    CommandExecutor<CheckNonExistentIrisRequest, CheckNonExistentIrisResponse> checkNonExistentIrisExecutor;
    @Mock
    EntityLinearizationService linearizationService;

    @Mock
    CommandExecutor<ValidateLogicalDefinitionFromApiRequest, ValidateLogicalDefinitionFromApiResponse> validateLogicalDefinitionsExecutor;

    private ValidatorService validatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validatorService = new ValidatorService(isExistingProjectExecutor, filterExistingEntitiesExecutor, getEntitySearchExecutor,
                validateEntityUpdateExecutor,
                tableConfigurationExecutor,
                entityTypesExecutor,
                checkNonExistentIrisExecutor,
                validateLogicalDefinitionsExecutor,
                linearizationService
              );
        when(getEntitySearchExecutor.execute(any(),any())).thenReturn(CompletableFuture.supplyAsync(() -> new GetExistingClassesForApiResponse(Page.emptyPage())));
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

    @Test
    void GIVEN_validLogicalConditions_WHEN_validateLogicalDefinitionsCalled_THEN_noExceptionThrown() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        LogicalConditionRelationship relationship = new LogicalConditionRelationship("http://example.com/axis", "http://example.com/filler");
        EntityLogicalDefinition logicalDefinition = new EntityLogicalDefinition(
                "http://example.com/superclass",
                List.of(relationship)
        );
        LogicalConditions logicalConditions = new LogicalConditions(
                List.of(logicalDefinition),
                List.of(relationship)
        );

        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ValidateLogicalDefinitionFromApiResponse(List.of())
                ));

        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateLogicalDefinitions(projectId, entityIri, logicalConditions));
    }

    @Test
    void GIVEN_nullLogicalConditions_WHEN_validateLogicalDefinitionsCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateLogicalDefinitions(projectId, entityIri, null),
                "Logical conditions cannot be null");
    }

    @Test
    void GIVEN_backendReturnsValidationErrors_WHEN_validateLogicalDefinitionsCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        LogicalConditionRelationship relationship = new LogicalConditionRelationship("http://example.com/axis", "http://example.com/filler");
        EntityLogicalDefinition logicalDefinition = new EntityLogicalDefinition(
                "http://example.com/superclass",
                List.of(relationship)
        );
        LogicalConditions logicalConditions = new LogicalConditions(
                List.of(logicalDefinition),
                List.of(relationship)
        );

        List<String> errorMessages = List.of("Error 1", "Error 2");
        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ValidateLogicalDefinitionFromApiResponse(errorMessages)
                ));

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateLogicalDefinitions(projectId, entityIri, logicalConditions));

        assertTrue(exception.getMessage().contains("Logical definitions validation failed"));
        assertTrue(exception.getMessage().contains("Error 1"));
        assertTrue(exception.getMessage().contains("Error 2"));
    }

    @Test
    void GIVEN_executorThrowsException_WHEN_validateLogicalDefinitionsCalled_THEN_throwsRuntimeException() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        LogicalConditions logicalConditions = new LogicalConditions(
                List.of(),
                List.of()
        );

        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new ExecutionException("Test error", null)));

        // WHEN & THEN
        assertThrows(RuntimeException.class,
                () -> validatorService.validateLogicalDefinitions(projectId, entityIri, logicalConditions));
    }

    @Test
    void GIVEN_validOWLEntityDtoWithLogicalConditions_WHEN_validateOWLEntityDtoCalled_THEN_noExceptionThrown() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";

        LogicalConditionRelationship relationship = new LogicalConditionRelationship("http://example.com/axis", "http://example.com/filler");
        EntityLogicalDefinition logicalDefinition = new EntityLogicalDefinition(
                "http://example.com/superclass",
                List.of(relationship)
        );
        LogicalConditions logicalConditions = new LogicalConditions(
                List.of(logicalDefinition),
                List.of(relationship)
        );

        EntityLogicalConditionsWrapper logicalConditionsWrapper = new EntityLogicalConditionsWrapper(
                logicalConditions,
                null
        );

        OWLEntityDto owlEntityDto = new OWLEntityDto(
                "http://example.com/entity",
                false,
                null,
                null,
                null,
                LocalDateTime.now(),
                null,
                null,
                logicalConditionsWrapper,
                null,
                null
        );

        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ValidateLogicalDefinitionFromApiResponse(List.of())
                ));

        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateOWLEntityDto(owlEntityDto, projectId));
    }

    @Test
    void GIVEN_nullOWLEntityDto_WHEN_validateOWLEntityDtoCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateOWLEntityDto(null, projectId),
                "OWLEntityDto cannot be null");
    }

    @Test
    void GIVEN_validBaseExclusionTerms_WHEN_validateBaseExclusionTermsCalled_THEN_noExceptionThrown() {
        // GIVEN
        List<BaseExclusionTerm> baseExclusionTerms = List.of(
                new BaseExclusionTerm("Label 1", "ref1", "term1"),
                new BaseExclusionTerm("Label 2", "ref2", "term2")
        );

        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateBaseExclusionTerms(baseExclusionTerms));
    }

    @Test
    void GIVEN_nullBaseExclusionTerms_WHEN_validateBaseExclusionTermsCalled_THEN_noExceptionThrown() {
        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateBaseExclusionTerms(null));
    }

    @Test
    void GIVEN_nullBaseExclusionTerm_WHEN_validateBaseExclusionTermsCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        List<BaseExclusionTerm> baseExclusionTerms = new ArrayList<>();
        baseExclusionTerms.add(null);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateBaseExclusionTerms(baseExclusionTerms),
                "BaseExclusionTerm cannot be null");
    }

    @Test
    void GIVEN_baseExclusionTermWithNullFoundationReference_WHEN_validateBaseExclusionTermsCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        BaseExclusionTerm term = new BaseExclusionTerm("Label", null, "term1");
        List<BaseExclusionTerm> baseExclusionTerms = List.of(term);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateBaseExclusionTerms(baseExclusionTerms),
                "BaseExclusionTerm has invalid foundationReference");
    }

    @Test
    void GIVEN_baseExclusionTermWithEmptyFoundationReference_WHEN_validateBaseExclusionTermsCalled_THEN_throwsIllegalArgumentException() {
        // GIVEN
        BaseExclusionTerm term = new BaseExclusionTerm("Label", "   ", "term1");
        List<BaseExclusionTerm> baseExclusionTerms = List.of(term);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> validatorService.validateBaseExclusionTerms(baseExclusionTerms),
                "BaseExclusionTerm has invalid foundationReference");
    }

    @Test
    void GIVEN_stringWithEscapeCharacters_WHEN_hasEscapeCharactersCalled_THEN_returnsTrue() {
        // GIVEN & WHEN & THEN
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\nNewLine"));
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\tTab"));
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\rReturn"));
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\\"Quote"));
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\'SingleQuote"));
        assertTrue(ValidatorService.hasEscapeCharacters("Test\\\\Backslash"));
    }

    @Test
    void GIVEN_stringWithoutEscapeCharacters_WHEN_hasEscapeCharactersCalled_THEN_returnsFalse() {
        // GIVEN & WHEN & THEN
        assertFalse(ValidatorService.hasEscapeCharacters("Normal String"));
        assertFalse(ValidatorService.hasEscapeCharacters("String with \\ at end\\"));
        assertFalse(ValidatorService.hasEscapeCharacters(""));
        assertFalse(ValidatorService.hasEscapeCharacters("String with backslash\\"));
    }

    @Test
    void GIVEN_logicalConditionsWithNullLists_WHEN_validateLogicalDefinitionsCalled_THEN_noExceptionThrown() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        LogicalConditions logicalConditions = new LogicalConditions(null, null);

        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ValidateLogicalDefinitionFromApiResponse(List.of())
                ));

        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateLogicalDefinitions(projectId, entityIri, logicalConditions));
    }

    @Test
    void GIVEN_logicalConditionsWithEmptyLists_WHEN_validateLogicalDefinitionsCalled_THEN_noExceptionThrown() {
        // GIVEN
        String projectId = "76bdeeb8-2d8e-4220-9185-0265af09c448";
        String entityIri = "http://example.com/entity";

        LogicalConditions logicalConditions = new LogicalConditions(List.of(), List.of());

        when(validateLogicalDefinitionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        new ValidateLogicalDefinitionFromApiResponse(List.of())
                ));

        // WHEN & THEN
        assertDoesNotThrow(() -> validatorService.validateLogicalDefinitions(projectId, entityIri, logicalConditions));
    }
}
