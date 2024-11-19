package edu.stanford.protege.gateway.ontology;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OwlOntologyServiceTest {


    EntityOntologyService service;

    @Mock
    private CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor;
    @Mock
    private CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;

    @Mock
    private CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;

    @Mock
    private CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;

    @Mock
    private CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;
    @Mock
    private CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;
    @Mock
    private CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor;

    private GetLogicalDefinitionsResponse response;

    private ProjectId projectId;

    private String entityIri;

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        objectMapper.configOverride(String.class)
                .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        File specFile = new File("src/test/resources/dummyLogicalDefinitionResponse.json");
        response = objectMapper.readValue(specFile, GetLogicalDefinitionsResponse.class);
        service = new EntityOntologyService(ancestorsExecutor, logicalDefinitionExecutor, formDataExecutor, entityChildrenExecutor, isExistingProjectExecutor, filterExistingEntitiesExecutor, createClassEntityExecutor);
        projectId = ProjectId.generate();
        entityIri = "http://id.who.int/icd/entity/257068234";
        when(logicalDefinitionExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> response));
    }


    @Test
    public void GIVEN_validLogicalDefinitionResponse_WHEN_mappingTheData_THEN_dataIsCorrectlyMapped() throws ExecutionException, InterruptedException, TimeoutException {
        EntityLogicalConditionsWrapper wrapper = service.getEntityLogicalConditions(entityIri, projectId.id()).get(1, TimeUnit.SECONDS);

        assertNotNull(wrapper);
        assertNotNull(wrapper.jsonRepresentation());
        assertEquals(1, wrapper.jsonRepresentation().logicalDefinitions().size());
        EntityLogicalDefinition logicalDefinition = wrapper.jsonRepresentation().logicalDefinitions().get(0);
        assertEquals("http://id.who.int/icd/entity/257068234", logicalDefinition.logicalDefinitionSuperclass());
        assertEquals(2, logicalDefinition.relationships().size());
        assertTrue(logicalDefinition.relationships().stream().noneMatch(r -> r.axis() == null || r.filler() == null));
        assertEquals(4, wrapper.jsonRepresentation().necessaryConditions().size());
        assertTrue(wrapper.jsonRepresentation().necessaryConditions().stream().noneMatch(n -> n.filler() == null || n.axis() == null));
        assertEquals(5, wrapper.functionalRepresentation().axioms().size());
    }
}
