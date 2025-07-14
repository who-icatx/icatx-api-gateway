package edu.stanford.protege.gateway.ontology;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.EntityLogicalConditionsWrapper;
import edu.stanford.protege.gateway.dto.EntityLogicalDefinition;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.projects.GetReproducibleProjectsRequest;
import edu.stanford.protege.gateway.projects.GetReproducibleProjectsResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OwlOntologyServiceTest {


    OntologyService service;

    @Mock
    private CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;

    @Mock
    private CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;

    @Mock
    private CommandExecutor<UpdateLogicalDefinitionsRequest, UpdateLogicalDefinitionsResponse> updateLogicalDefinitionExecutor;

    @Mock
    private CommandExecutor<ChangeEntityParentsRequest, ChangeEntityParentsResponse> updateParentsExecutor;

    @Mock
    private CommandExecutor<SetEntityFormDataFromJsonRequest, SetEntityFormDataFromJsonResponse> updateLanguageTermsExecutor;

    @Mock
    private CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;

    @Mock
    private CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor;

    @Mock
    private CommandExecutor<GetAvailableProjectsForApiRequest, GetAvailableProjectsForApiResponse> getAvailableProjectsExecutor;

    @Mock
    private CommandExecutor<GetEntityCommentsRequest, GetEntityCommentsResponse> entityDiscussionExecutor;

    @Mock
    private CommandExecutor<GetClassHierarchyParentsByAxiomTypeRequest, GetClassHierarchyParentsByAxiomTypeResponse> classHierarchyParents;

    @Mock
    private CommandExecutor<GetReproducibleProjectsRequest, GetReproducibleProjectsResponse> reproducibleProjectsExecutor;
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
        service = new OntologyService(classHierarchyParents, logicalDefinitionExecutor,
                formDataExecutor,
                entityChildrenExecutor,
                createClassEntityExecutor,
                getAvailableProjectsExecutor,
                entityDiscussionExecutor,
                updateLogicalDefinitionExecutor,
                updateParentsExecutor,
                updateLanguageTermsExecutor, reproducibleProjectsExecutor);
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
