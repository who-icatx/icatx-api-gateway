package edu.stanford.protege.gateway.ontology;


import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EntityOntologyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityOntologyService.class);
    private final CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor;
    private final CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;
    private final CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;
    private final CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor;
    private final CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor;
    private final CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor;
    private final CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor;


    public EntityOntologyService(CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor,
                                 CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor,
                                 CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor,
                                 CommandExecutor<GetEntityChildrenRequest, GetEntityChildrenResponse> entityChildrenExecutor,
                                 CommandExecutor<GetIsExistingProjectRequest, GetIsExistingProjectResponse> isExistingProjectExecutor,
                                 CommandExecutor<FilterExistingEntitiesRequest, FilterExistingEntitiesResponse> filterExistingEntitiesExecutor,
                                 CommandExecutor<CreateClassesFromApiRequest, CreateClassesFromApiResponse> createClassEntityExecutor) {
        this.ancestorsExecutor = ancestorsExecutor;
        this.logicalDefinitionExecutor = logicalDefinitionExecutor;
        this.formDataExecutor = formDataExecutor;
        this.entityChildrenExecutor = entityChildrenExecutor;
        this.isExistingProjectExecutor = isExistingProjectExecutor;
        this.filterExistingEntitiesExecutor = filterExistingEntitiesExecutor;
        this.createClassEntityExecutor = createClassEntityExecutor;
    }


    public CompletableFuture<List<String>> getEntityParents(String entityIri, String projectId) {

        return ancestorsExecutor.execute(new GetClassAncestorsRequest(IRI.create(entityIri), ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(response ->
                        response.getAncestorClassHierarchy().getChildren().stream().map(child -> child.getNode().getEntity().getIRI().toString())
                                .collect(Collectors.toList()));

    }

    public CompletableFuture<EntityLogicalConditionsWrapper> getEntityLogicalConditions(String entityIri, String projectId) {
        return logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), SecurityContextHelper.getExecutionContext())
                .thenApply(response ->
                        new EntityLogicalConditionsWrapper(new LogicalConditions(mapToEntityLogicalDefinition(response.logicalDefinitions()),
                                extractRelationshipsFromPropertyClassValue(response.necessaryConditions())),
                                new LogicalConditionsFunctionalOwl("OWLFunctionalSyntax", response.functionalAxioms()))
                );

    }

    public CompletableFuture<EntityLanguageTerms> getEntityLanguageTerms(String entityIri, String projectId, String formId) {
        return formDataExecutor.execute(new GetEntityFormAsJsonRequest(ProjectId.valueOf(projectId), entityIri, formId), SecurityContextHelper.getExecutionContext())
                .thenApply(formResponse -> EntityFormToDtoMapper.mapFormToTerms(formResponse.form()));

    }

    private List<EntityLogicalDefinition> mapToEntityLogicalDefinition(List<LogicalDefinition> logicalDefinitions) {
        return logicalDefinitions.stream().map(definition -> {
            List<LogicalConditionRelationship> relationships = extractRelationshipsFromPropertyClassValue(definition.axis2filler());
            return new EntityLogicalDefinition(definition.logicalDefinitionParent().getEntity().getIRI().toString(), relationships);
        }).collect(Collectors.toList());
    }

    private List<LogicalConditionRelationship> extractRelationshipsFromPropertyClassValue(List<PropertyClassValue> values) {
        List<LogicalConditionRelationship> relationships = new ArrayList<>();
        Map<String, List<String>> axisFillerMap = new HashMap<>();
        for (PropertyClassValue propertyClassValue : values) {
            List<String> existingFillers = axisFillerMap.get(propertyClassValue.getProperty().getEntity().getIRI().toString());
            if (existingFillers == null) {
                existingFillers = new ArrayList<>();
            }
            existingFillers.add(propertyClassValue.getValue().getEntity().getIRI().toString());
            axisFillerMap.put(propertyClassValue.getProperty().getEntity().getIRI().toString(), existingFillers);
        }
        axisFillerMap.keySet().forEach(key -> relationships.addAll(axisFillerMap.get(key).stream()
                .map(filler -> new LogicalConditionRelationship(key, filler)).toList())
        );
        return relationships;
    }

    public CompletableFuture<List<String>> getEntityChildren(String entityIri, String projectId) {
        return entityChildrenExecutor.execute(GetEntityChildrenRequest.create(IRI.create(entityIri), ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.childrenIris()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toList())
                );
    }

    public CompletableFuture<Boolean> isExistingProject(String projectId) {
        return isExistingProjectExecutor.execute(GetIsExistingProjectRequest.create(ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                .thenApply(GetIsExistingProjectResponse::isExistingProject);
    }

    public CompletableFuture<Set<String>> getExistingEntities(String projectId, List<String> entityParents) {
        var iriSet = entityParents.stream().map(IRI::create).collect(Collectors.toSet());
        return filterExistingEntitiesExecutor.execute(FilterExistingEntitiesRequest.create(ProjectId.valueOf(projectId), iriSet), SecurityContextHelper.getExecutionContext())
                .thenApply(
                        response -> response.existingEntities()
                                .stream()
                                .map(IRI::toString)
                                .collect(Collectors.toSet())
                );
    }

    public CompletableFuture<Set<String>> createClassEntity(String projectId, CreateEntityDto createEntityDto) {
        var entityParentsSet = createEntityDto.entityParents().stream().collect(ImmutableSet.toImmutableSet());
        return createClassEntityExecutor.execute(
                CreateClassesFromApiRequest.create(
                        ChangeRequestId.generate(),
                        ProjectId.valueOf(projectId),
                        createEntityDto.entityName(),
                        createEntityDto.languageTag(),
                        entityParentsSet
                ),
                SecurityContextHelper.getExecutionContext()
        ).thenApply(
                CreateClassesFromApiResponse::newEntityIris
        );
    }
}
