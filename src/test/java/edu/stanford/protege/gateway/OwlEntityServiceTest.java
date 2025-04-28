package edu.stanford.protege.gateway;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.history.EntityHistoryService;
import edu.stanford.protege.gateway.linearization.EntityLinearizationService;
import edu.stanford.protege.gateway.ontology.OntologyService;
import edu.stanford.protege.gateway.postcoordination.EntityPostCoordinationService;
import edu.stanford.protege.gateway.validators.ValidatorService;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwlEntityServiceTest {

    private OWLEntityDto dto;
    @Mock
    private EntityLinearizationService entityLinearizationService;
    @Mock
    private EntityPostCoordinationService entityPostCoordinationService;
    @Mock
    private OntologyService entityOntologyService;
    @Mock
    private EntityHistoryService entityHistoryService;
    @Mock
    EntityLinearizationWrapperDto linearizationWrapperDto;
    @Mock
    EntityLanguageTerms entityLanguageTerms;

    @Mock
    private EventDispatcher eventDispatcher;
    @Mock
    EntityLogicalConditionsWrapper entityLogicalDefinition;
    @Mock
    ValidatorService validatorService;

    private OwlEntityService service;

    private final String existingProjectId = "b717d9a3-f265-46f5-bd15-9f1cf4b132c8";

    private final LocalDateTime latestUpdate = LocalDateTime.of(2024, 1, 1, 1, 1);

    private final String eTag = Hashing.sha256().hashString(latestUpdate.toString(), StandardCharsets.UTF_8).toString();

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        objectMapper.configOverride(String.class)
                .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        File specFile = new File("src/test/resources/dummyOwlEntityDto.json");
        dto = objectMapper.readValue(specFile, OWLEntityDto.class);

        service = new OwlEntityService(
                entityLinearizationService,
                entityPostCoordinationService,
                entityHistoryService,
                eventDispatcher,
                entityOntologyService,
                validatorService);
    }

    private void initializeGetMocks() {
        when(entityLanguageTerms.title()).thenReturn(dto.languageTerms().title());

        when(entityLinearizationService.getEntityLinearizationDto(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> linearizationWrapperDto));
        when(entityPostCoordinationService.getPostCoordinationSpecifications(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityPostCoordinationService.getEntityCustomScales(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityOntologyService.getEntityLanguageTerms(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> entityLanguageTerms));
        when(entityOntologyService.getEntityLogicalConditions(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> entityLogicalDefinition));
        when(entityOntologyService.getEntityParents(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityHistoryService.getEntityLatestChangeTime(any(), any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> this.latestUpdate));
    }

    @Test
    public void GIVEN_validEntity_WHEN_getEntityInfo_THEN_returnsCorrectDto() {
        initializeGetMocks();

        OWLEntityDto result = service.getEntityInfo(dto.entityIRI(), existingProjectId);
        EntityLanguageTermsDto termDto = EntityLanguageTermsDto.getFromTerms(entityLanguageTerms);
        assertNotNull(result);
        assertEquals(dto.entityIRI(), result.entityIRI());
        assertEquals(dto.languageTerms().title().label(), result.languageTerms().title().label());
        assertEquals(dto.isObsolete(), result.isObsolete());
        assertSame(linearizationWrapperDto, result.entityLinearizations());
        assertEquals(termDto,      result.languageTerms());
        assertSame(entityLogicalDefinition,  result.logicalConditions());
        assertEquals(latestUpdate, result.lastChangeDate());

        verify(validatorService, times(1)).validateProjectId(existingProjectId);
        verify(validatorService, times(1)).validateEntityExists(existingProjectId, dto.entityIRI());
    }

    @Test
    public void GIVEN_validEntity_WHEN_getEntityInfo_THEN_returnsCorrectDto() {
        initializeGetMocks();

        OWLEntityDto result = service.getEntityInfo(dto.entityIRI(), existingProjectId);

        assertNotNull(result);
        assertEquals(dto.entityIRI(), result.entityIRI());
        assertEquals(dto.languageTerms().title().label(), result.languageTerms().title().label());
        assertSame(linearizationWrapperDto, result.entityLinearizations());
        assertSame(entityLanguageTerms,      result.languageTerms());
        assertSame(entityLogicalDefinition,  result.logicalConditions());
        assertEquals(latestUpdate, result.lastChangeDate());

        verify(validatorService, times(1)).validateProjectId(existingProjectId);
        verify(validatorService, times(1)).validateEntityExists(existingProjectId, dto.entityIRI());
    }

    @Test
    public void GIVEN_missingTitle_WHEN_getEntityInfo_THEN_throwsEntityIsMissingException() {
        initializeGetMocks();
        when(entityLanguageTerms.title()).thenReturn(new LanguageTerm(null, ""));

        EntityIsMissingException ex = assertThrows(EntityIsMissingException.class,
                () -> service.getEntityInfo(dto.entityIRI(), existingProjectId));

        assertEquals("Entity with iri " + dto.entityIRI() + " is missing", ex.getMessage());
    }
    @Test
    public void GIVEN_missingTitle_WHEN_getEntityInfo_THEN_throwsEntityIsMissingException() {
        initializeGetMocks();
        when(entityLanguageTerms.title()).thenReturn(new LanguageTerm(null, ""));

        EntityIsMissingException ex = assertThrows(EntityIsMissingException.class,
                () -> service.getEntityInfo(dto.entityIRI(), existingProjectId));

        assertEquals("Entity with iri " + dto.entityIRI() + " is missing", ex.getMessage());
    }

    @Test
    public void GIVEN_entityWithLinearizationParentDifferentThanExistingParents_WHEN_update_THEN_validationIsThrown() {
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));

        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId)))
                .thenReturn(CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690")));

        dto.entityLinearizations().linearizations().add(new EntityLinearization("UNKNOWN", "FALSE", "UNKNOWN", "potatoParent", "http://id.who.int/icd/release/11/ocu", null));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateEntity(dto, existingProjectId, eTag));

        assertEquals("Entity has a linearization with parent potatoParent that is not in the available parents [http://id.who.int/icd/entity/1553463690]", exception.getMessage());
    }

    @Test
    public void GIVEN_entityThatIsPresentAsOwnParent_WHEN_update_THEN_validationExceptionIsThrown() {
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));

        dto.parents().add(dto.entityIRI());
        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateEntity(dto, existingProjectId, eTag));

        assertEquals("Entity contains in the parents its own parents", exception.getMessage());
    }

    @Test
    public void GIVEN_callWithDifferentHash_WHEN_update_THEN_versionDoesNotMatchExceptionIsThrown() {
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));

        VersionDoesNotMatchException exception = assertThrows(VersionDoesNotMatchException.class,
                () -> service.updateEntity(dto, existingProjectId, "PotatoTag"));

        assertEquals("Received version out of date : Received hash PotatoTag is different from " + eTag, exception.getMessage());
    }

    @Test
    public void GIVEN_getCall_WHEN_titleIsMissing_THEN_exceptionIsThrown() {
        initializeGetMocks();
        when(entityLanguageTerms.title()).thenReturn(new LanguageTerm(null, null));

        EntityIsMissingException exception = assertThrows(EntityIsMissingException.class,
                () -> service.getEntityInfo(dto.entityIRI(), existingProjectId));

        assertEquals("Entity with iri " + dto.entityIRI() + " is missing", exception.getMessage());
    }

    @Test
    public void GIVEN_aValidUpdateRequest_WHEN_update_THEN_serviceIsCalled() {
        initializeGetMocks();
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));
        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId)))
                .thenReturn(CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690")));

        service.updateEntity(dto, existingProjectId, eTag);

        verify(entityLinearizationService, times(1)).updateEntityLinearization(eq(dto), eq(ProjectId.valueOf(existingProjectId)), any());
        verify(entityPostCoordinationService, times(1)).updateEntityPostCoordination(any(), any(), any(), any());
        verify(entityOntologyService, times(1)).updateLanguageTerms(any(), any(), any(), any(), any());
        verify(entityOntologyService, times(1)).updateEntityParents(any(), any(), any(), any());
        verify(entityOntologyService, times(1)).updateLogicalDefinition(any(), any(), any(), any());
    }

    @Test
    public void GIVEN_validRequest_WHEN_callUpdate_THEN_entityUpdatedSuccessfullyIsEmitted() {
        initializeGetMocks();
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));
        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId)))
                .thenReturn(CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690")));

        service.updateEntity(dto, existingProjectId, eTag);
        verify(eventDispatcher, times(1)).dispatchEvent(any(EntityUpdatedSuccessfullyEvent.class), any());
    }

    @Test
    public void GIVEN_applicationExceptionFromLinearization_WHEN_callUpdate_THEN_entityUpdateFailedEventIsEmitted() {
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI())))
                .thenReturn(CompletableFuture.supplyAsync(() -> latestUpdate));
        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId)))
                .thenReturn(CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690")));

        doThrow(new ApplicationException("Error"))
                .when(entityLinearizationService).updateEntityLinearization(any(), eq(ProjectId.valueOf(existingProjectId)), any());

        assertThrows(ApplicationException.class, () -> service.updateEntity(dto, existingProjectId, eTag));
        verify(eventDispatcher, times(1)).dispatchEvent(any(EntityUpdateFailedEvent.class), any());
    }
}
