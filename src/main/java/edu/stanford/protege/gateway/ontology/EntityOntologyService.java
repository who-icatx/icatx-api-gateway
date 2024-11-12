package edu.stanford.protege.gateway.ontology;


import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EntityOntologyService {
    private final CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor;
    private final CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;

    private final CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;

    public EntityOntologyService(CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor, CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor, CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor) {
        this.ancestorsExecutor = ancestorsExecutor;
        this.logicalDefinitionExecutor = logicalDefinitionExecutor;
        this.formDataExecutor = formDataExecutor;
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
}
