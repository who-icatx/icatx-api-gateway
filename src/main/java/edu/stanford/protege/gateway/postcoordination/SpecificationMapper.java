package edu.stanford.protege.gateway.postcoordination;

import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpecificationMapper {

    public static List<EntityPostCoordinationSpecificationDto> mapFromResponse(GetEntityPostCoordinationResponse response, List<LinearizationDefinition> definitions) {
        List<EntityPostCoordinationSpecificationDto> resp = new ArrayList<>();
        for (PostCoordinationSpecification specification : response.postCoordinationSpecification().postcoordinationSpecifications()) {
            LinearizationDefinition definition = gedDefinitionByView(specification.getLinearizationView(), definitions);
            EntityPostCoordinationSpecificationDto dto = new EntityPostCoordinationSpecificationDto(definition.getId(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
            if (isMainAxis(definition)) {
                mapAsPrimary(specification, dto);
            } else {
                mapAsTelescopic(specification, dto);
            }
            if (!dto.requiredAxes().isEmpty() ||
                    !dto.allowedAxes().isEmpty() ||
                    !dto.notAllowedAxes().isEmpty() ||
                    !dto.overwrittenNotAllowedAxes().isEmpty() ||
                    !dto.overwrittenAllowedAxes().isEmpty() ||
                    !dto.overwrittenRequiredAxes().isEmpty()) {
                resp.add(dto);
            }
        }

        return resp;
    }

    public static WhoficEntityPostCoordinationSpecification mapFromDtoList(String entityIri,
                                                                           String entityType,
                                                                           List<EntityPostCoordinationSpecificationDto> dtos,
                                                                           List<LinearizationDefinition> definitions,
                                                                           TableConfiguration tableConfiguration) {

        List<PostCoordinationSpecification> specifications = dtos.stream().map(dto -> {
            LinearizationDefinition definition = getDefinitionByLinearizationId(dto.linearizationId(), definitions);
            PostCoordinationSpecification spec = new PostCoordinationSpecification(definition.getWhoficEntityIri(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());

            spec.getAllowedAxes().addAll(dto.allowedAxes());
            spec.getAllowedAxes().addAll(dto.overwrittenAllowedAxes());
            spec.getRequiredAxes().addAll(dto.requiredAxes());
            spec.getRequiredAxes().addAll(dto.overwrittenRequiredAxes());
            spec.getNotAllowedAxes().addAll(dto.notAllowedAxes());
            spec.getNotAllowedAxes().addAll(dto.overwrittenNotAllowedAxes());


            List<String> allAvailableAxis = new ArrayList<>(tableConfiguration.postCoordinationAxes());
            allAvailableAxis.addAll(new ArrayList<>(tableConfiguration.compositePostCoordinationAxes().stream().map(CompositeAxis::postCoordinationAxis)
                    .collect(Collectors.toList())));

            allAvailableAxis.removeAll(spec.getAllowedAxes());
            allAvailableAxis.removeAll(spec.getRequiredAxes());
            allAvailableAxis.removeAll(spec.getNotAllowedAxes());
            if(definition.getCoreLinId() == null || definition.getCoreLinId().isEmpty()) {
                spec.getNotAllowedAxes().addAll(allAvailableAxis);
            } else {
                spec.getDefaultAxes().addAll(allAvailableAxis);
            }
            return spec;
        }).toList();

        return new WhoficEntityPostCoordinationSpecification(entityIri, entityType, specifications);
    }


    private static void mapAsTelescopic(PostCoordinationSpecification specification, EntityPostCoordinationSpecificationDto dto) {
        dto.overwrittenAllowedAxes().addAll(specification.getAllowedAxes());
        dto.overwrittenRequiredAxes().addAll(specification.getRequiredAxes());
        dto.overwrittenNotAllowedAxes().addAll(specification.getNotAllowedAxes());
    }

    private static void mapAsPrimary(PostCoordinationSpecification specification, EntityPostCoordinationSpecificationDto dto) {
        dto.allowedAxes().addAll(specification.getAllowedAxes());
        dto.notAllowedAxes().addAll(specification.getNotAllowedAxes());
        dto.requiredAxes().addAll(specification.getRequiredAxes());
    }

    private static boolean isMainAxis(LinearizationDefinition linearizationDefinition) {
        return linearizationDefinition.getCoreLinId() == null || linearizationDefinition.getCoreLinId().isEmpty();
    }

    private static LinearizationDefinition getDefinitionByLinearizationId(String linearizationId, List<LinearizationDefinition> definitions) {
        return definitions.stream().filter(def -> def.getId().equalsIgnoreCase(linearizationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error finding definition with id " + linearizationId));
    }

    private static LinearizationDefinition gedDefinitionByView(String linearizationView, List<LinearizationDefinition> definitions) {
        return definitions.stream().filter(def -> def.getWhoficEntityIri().equalsIgnoreCase(linearizationView))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error finding definition with id " + linearizationView));
    }
}
