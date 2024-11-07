package edu.stanford.protege.gateway.ontology;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.ontology.commands.*;
import edu.stanford.protege.gateway.ontology.commands.OntologicalLogicalConditions;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EntityOntologyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EntityOntologyService.class);
    private final CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor;
    private final CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor;

    private final CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor;

    public EntityOntologyService(CommandExecutor<GetClassAncestorsRequest, GetClassAncestorsResponse> ancestorsExecutor, CommandExecutor<GetLogicalDefinitionsRequest, GetLogicalDefinitionsResponse> logicalDefinitionExecutor, CommandExecutor<GetEntityFormAsJsonRequest, GetEntityFormAsJsonResponse> formDataExecutor) {
        this.ancestorsExecutor = ancestorsExecutor;
        this.logicalDefinitionExecutor = logicalDefinitionExecutor;
        this.formDataExecutor = formDataExecutor;
    }


    public List<String> getEntityParents(String entityIri, String projectId) {
        try {
            return ancestorsExecutor.execute(new GetClassAncestorsRequest(IRI.create(entityIri), ProjectId.valueOf(projectId)), SecurityContextHelper.getExecutionContext())
                    .get().getAncestorClassHierarchy().getChildren().stream().map(child -> child.getNode().getEntity().getIRI().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error while fetching ancestors ", e);
        }
        return new ArrayList<>();
    }

    public EntityLogicalConditionsWrapper getEntityLogicalConditions(String entityIri, String projectId) {
        try {
            GetLogicalDefinitionsResponse response = logicalDefinitionExecutor.execute(new GetLogicalDefinitionsRequest(ProjectId.valueOf(projectId), new OWLClassImpl(IRI.create(entityIri))), SecurityContextHelper.getExecutionContext())
                    .get();

            return new EntityLogicalConditionsWrapper(new LogicalConditions(getLogicalDefinitions(response.logicalDefinitions()),
                    extractRelationshipsFromPropertyClassValue(response.necessaryConditions())),
                    new LogicalConditionsFunctionalOwl("OWLFunctionalSyntax", response.functionalAxioms()));
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while fetching logical definition ", e);
        }
        return new EntityLogicalConditionsWrapper(new LogicalConditions(new ArrayList<>(), new ArrayList<>()), new LogicalConditionsFunctionalOwl("", new ArrayList<>()));
    }

    public EntityLanguageTerms getEntityLanguageTerms(String entityIri, String projectId, String formId) {
        try {
            GetEntityFormAsJsonResponse formResponse = formDataExecutor.execute(new GetEntityFormAsJsonRequest(ProjectId.valueOf(projectId), entityIri, formId), SecurityContextHelper.getExecutionContext())
                    .get();

            return EntityFormToDtoMapper.mapFormToTerms(formResponse.form());
        } catch (Exception e) {
            LOGGER.error("Error while fetching logical definition ", e);
            throw new RuntimeException(e);
        }
    }

    private List<EntityLogicalDefinition> getLogicalDefinitions(List<LogicalDefinition> logicalDefinitions) {
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
            if(existingFillers == null) {
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
