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
import edu.stanford.protege.webprotege.common.ProjectId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
    EntityLogicalConditionsWrapper entityLogicalDefinition;

    private OwlEntityService service;

    private final String existingProjectId = "b717d9a3-f265-46f5-bd15-9f1cf4b132c8";

    private final LocalDateTime latestUpdate =  LocalDateTime.of(2024, 1, 1, 1, 1);

    private final String eTag =  Hashing.sha256().hashString(latestUpdate.toString(), StandardCharsets.UTF_8).toString();

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        objectMapper.configOverride(String.class)
                .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        File specFile = new File("src/test/resources/dummyOwlEntityDto.json");
        dto = objectMapper.readValue(specFile, OWLEntityDto.class);
        service = new OwlEntityService(entityLinearizationService, entityPostCoordinationService,entityHistoryService, entityOntologyService);

    }

    private void initializeGetMocks() {
        when(entityLanguageTerms.title()).thenReturn(dto.languageTerms().title());
        when(entityLinearizationService.getEntityLinearizationDto(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> linearizationWrapperDto));
        when(entityPostCoordinationService.getPostCoordinationSpecifications(any(), any())).thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityPostCoordinationService.getEntityCustomScales(any(), any())).thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityOntologyService.getEntityLanguageTerms(any(), any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> entityLanguageTerms));
        when(entityOntologyService.getEntityLogicalConditions(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> entityLogicalDefinition));
        when(entityOntologyService.getEntityParents(any(), any())).thenReturn(CompletableFuture.supplyAsync(ArrayList::new));
        when(entityHistoryService.getEntityLatestChangeTime(any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> this.latestUpdate));
    }

    @Test
    public void GIVEN_entityThatHasMissingHistory_WHEN_update_THEN_validationExceptionIsThrown(){
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI()))).thenReturn(
                CompletableFuture.supplyAsync(() -> LocalDateTime.MIN));

        dto.parents().add(dto.entityIRI());
        EntityIsMissingException exception  = assertThrows(EntityIsMissingException.class, () -> service.updateEntity(dto, existingProjectId, eTag));

        assertEquals("Entity with iri " + dto.entityIRI() +" is missing", exception.getMessage());
    }


    @Test
    public void GIVEN_entityWithLinearizationParentDifferentThanExistingParents_WHEN_update_THEN_validationIsThrown(){
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI()))).thenReturn(
                CompletableFuture.supplyAsync(() -> LocalDateTime.of(2024, 1, 1, 1, 1))
        );

        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId))).thenReturn(
                CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690"))
        );

        dto.entityLinearizations().linearizations().add(new EntityLinearization("UNKNOWN",
                "FALSE",
                "UNKNOWN",
                "potatoParent",
                "http://id.who.int/icd/release/11/ocu",
                        null));

        ValidationException exception  = assertThrows(ValidationException.class, () -> service.updateEntity(dto, existingProjectId, eTag));

        assertEquals("Entity has a linearization with parent potatoParent that is not in the available parents [http://id.who.int/icd/entity/1553463690]", exception.getMessage());
    }

    @Test
    public void GIVEN_entityThatIsPresentAsOwnParent_WHEN_update_THEN_validationExceptionIsThrown() {
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI()))).thenReturn(
                CompletableFuture.supplyAsync(() -> LocalDateTime.of(2024, 1, 1, 1, 1))
        );

        dto.parents().add(dto.entityIRI());
        ValidationException exception  = assertThrows(ValidationException.class, () -> service.updateEntity(dto, existingProjectId, eTag));

        assertEquals("Entity contains in the parents its own parents", exception.getMessage());
    }

    @Test
    public void GIVEN_callWithDifferentHash_WHEN_update_THEN_versionDoesNotMatchExceptionIsThrown(){
        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI()))).thenReturn(
                CompletableFuture.supplyAsync(() -> LocalDateTime.of(2024, 1, 1, 1, 1))
        );

        VersionDoesNotMatchException exception  = assertThrows(VersionDoesNotMatchException.class, () -> service.updateEntity(dto, existingProjectId, "PotatoTag"));

        assertEquals("Received hash PotatoTag is different from cd78e6f5802bff1df5f43103eb17e1b2cf17a3f4cf9b182e7d0194eb112ab3df", exception.getMessage());

    }

    @Test
    public void GIVEN_getCall_WHEN_titleIsMissing_THEN_exceptionIsThrown(){
        initializeGetMocks();
        when(entityLanguageTerms.title()).thenReturn(new LanguageTerm(null, null));
        EntityIsMissingException exception  = assertThrows(EntityIsMissingException.class, () -> service.getEntityInfo(dto.entityIRI(), existingProjectId));

        assertEquals("Entity with iri http://id.who.int/icd/entity/1855860109 is missing", exception.getMessage());

    }

    @Test
    public void GIVEN_aValidUpdateRequest_WHEN_update_THEN_serviceIsCalled(){
        initializeGetMocks();

        when(entityHistoryService.getEntityLatestChangeTime(eq(existingProjectId), eq(dto.entityIRI()))).thenReturn(
                CompletableFuture.supplyAsync(() -> LocalDateTime.of(2024, 1, 1, 1, 1))
        );

        when(entityOntologyService.getEntityParents(eq(dto.entityIRI()), eq(existingProjectId))).thenReturn(
                CompletableFuture.supplyAsync(() -> Arrays.asList("http://id.who.int/icd/entity/1553463690"))
        );

        service.updateEntity(dto, existingProjectId, eTag);

        verify(entityLinearizationService, times(1)).updateEntityLinearization(eq(dto), eq(ProjectId.valueOf(existingProjectId)));
        verify(entityPostCoordinationService, times(1)).updateEntityPostCoordination(any(), any(), any());
        verify(entityOntologyService, times(1)).updateLanguageTerms(any(), any(), any(), any());
        verify(entityOntologyService, times(1)).updateEntityParents(any(), any(), any());
        verify(entityOntologyService, times(1)).updateLogicalDefinition(any(), any(), any());
    }
}