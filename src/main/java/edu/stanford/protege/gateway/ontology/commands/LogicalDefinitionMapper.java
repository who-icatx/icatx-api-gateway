package edu.stanford.protege.gateway.ontology.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.stanford.protege.gateway.dto.EntityLogicalDefinition;
import edu.stanford.protege.gateway.dto.LogicalConditionRelationship;
import edu.stanford.protege.gateway.dto.LogicalConditions;
import edu.stanford.protege.webprotege.common.ShortForm;
import edu.stanford.protege.webprotege.entity.OWLClassData;
import edu.stanford.protege.webprotege.entity.OWLObjectPropertyData;
import edu.stanford.protege.webprotege.frame.PropertyClassValue;
import edu.stanford.protege.webprotege.frame.State;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogicalDefinitionMapper {

    public static List<EntityLogicalDefinition> mapToEntityLogicalDefinition(List<LogicalDefinition> logicalDefinitions) {
        return logicalDefinitions.stream().map(definition -> {
            List<LogicalConditionRelationship> relationships = extractRelationshipsFromPropertyClassValue(definition.axis2filler());
            return new EntityLogicalDefinition(definition.logicalDefinitionParent().getEntity().getIRI().toString(), relationships);
        }).collect(Collectors.toList());
    }

    public static List<LogicalConditionRelationship> extractRelationshipsFromPropertyClassValue(List<PropertyClassValue> values) {
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


    public static OntologicalLogicalDefinitionConditions mapFromDto(LogicalConditions logicalConditions) {
        List<LogicalDefinition> logicalDefinitions = logicalConditions.logicalDefinitions().stream()
                .map(def -> {
                    List<PropertyClassValue> values = getValuesFromLogicalConditionRelationship(def.relationships());
                    ImmutableList<ShortForm> shortForms = ImmutableList.of();

                    return new LogicalDefinition(OWLClassData.get(new OWLClassImpl(IRI.create(def.logicalDefinitionSuperclass())), shortForms, false), values);
                }).toList();
        List<PropertyClassValue> necessaryConditions = getValuesFromLogicalConditionRelationship(logicalConditions.necessaryConditions());
        return new OntologicalLogicalDefinitionConditions(logicalDefinitions, necessaryConditions);
    }

    private static List<PropertyClassValue> getValuesFromLogicalConditionRelationship(List<LogicalConditionRelationship> relationships) {
        return relationships.stream().map(rel -> {
            ImmutableList<ShortForm> shortForms = ImmutableList.of();
            OWLObjectPropertyData propertyData = OWLObjectPropertyData.get(new OWLObjectPropertyImpl(IRI.create(rel.axis())), shortForms, false);
            OWLClassData owlClassData = OWLClassData.get(new OWLClassImpl(IRI.create(rel.filler())), ImmutableList.of(), false);
            return PropertyClassValue.get(propertyData, owlClassData, State.ASSERTED);

        }).collect(Collectors.toList());
    }




}
