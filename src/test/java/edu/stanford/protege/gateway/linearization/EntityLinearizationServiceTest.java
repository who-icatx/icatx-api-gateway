package edu.stanford.protege.gateway.linearization;


import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.EntityLinearization;
import edu.stanford.protege.gateway.dto.EntityLinearizationWrapperDto;
import edu.stanford.protege.gateway.linearization.commands.GetEntityLinearizationsRequest;
import edu.stanford.protege.gateway.linearization.commands.GetEntityLinearizationsResponse;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinitionRequest;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinitionResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntityLinearizationServiceTest {

    @Mock
    private CommandExecutor<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> entityLinearizationCommandMock;
    @Mock
    private CommandExecutor<LinearizationDefinitionRequest, LinearizationDefinitionResponse> definitionExecutor;

    private EntityLinearizationService service;

    private String projectId;

    private String entityIri;


    @BeforeEach
    public void setUp() throws IOException {
        projectId = ProjectId.generate().id();
        entityIri = "http://id.who.int/icd/entity/257068234";

        File initialFile = new File("src/test/resources/dummyLinearizationResponse.json");
        GetEntityLinearizationsResponse response = new ApplicationBeans().objectMapper().readValue(initialFile, GetEntityLinearizationsResponse.class);
        when(entityLinearizationCommandMock.execute(eq(new GetEntityLinearizationsRequest(entityIri, ProjectId.valueOf(projectId))), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> response));

        service = new EntityLinearizationService(entityLinearizationCommandMock, definitionExecutor);
    }


    @Test
    public void GIVEN_entity_WHEN_fetchLinearization_THEN_responseIsCorrectlyMapped() throws ExecutionException, InterruptedException {
        EntityLinearizationWrapperDto dto = service.getEntityLinearizationDto(entityIri, projectId).get();
        assertNotNull(dto);
        assertEquals("true", dto.suppressUnspecifiedResiduals());
        assertEquals("false", dto.suppressOtherSpecifiedResiduals());
        assertEquals("This is a parent title", dto.otherSpecifiedResidualTitle().label());
        assertNotNull(dto.linearizations());
        assertEquals(2, dto.linearizations().size());
        Optional<EntityLinearization> mms = getLinearizationById(dto.linearizations(), "http://id.who.int/icd/release/11/mms");
        assertTrue(mms.isPresent());
        assertEquals("unknown", mms.get().isAuxiliaryAxisChild());
        assertEquals("false", mms.get().isGrouping());
        assertEquals("true", mms.get().isIncludedInLinearization());
        assertEquals("http://id.who.int/icd/entity/135352227", mms.get().linearizationPathParent());

        Optional<EntityLinearization> der = getLinearizationById(dto.linearizations(), "http://id.who.int/icd/release/11/der");
        assertTrue(der.isPresent());
        assertEquals("unknown", der.get().isAuxiliaryAxisChild());
        assertEquals("false", der.get().isGrouping());
        assertEquals("unknown", der.get().isIncludedInLinearization());
        assertEquals("", der.get().linearizationPathParent());
        assertEquals("this is a coding note", der.get().codingNote());
    }

    private Optional<EntityLinearization> getLinearizationById(List<EntityLinearization> linearizationList, String linearizationId) {
        return linearizationList.stream().filter(l -> l.linearizationId().equalsIgnoreCase(linearizationId))
                .findFirst();
    }
}
