package edu.stanford.protege.gateway.postcoordination;

import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationResponse;
import edu.stanford.protege.gateway.postcoordination.commands.PostCoordinationSpecification;

import java.util.ArrayList;
import java.util.List;

public class SpecificationMapper {

    public static List<EntityPostCoordinationSpecificationDto> mapFromResponse(GetEntityPostCoordinationResponse response, List<LinearizationDefinition> definitions) {
        List<EntityPostCoordinationSpecificationDto> resp = new ArrayList<>();
        for(PostCoordinationSpecification specification : response.postCoordinationSpecification().postcoordinationSpecifications()) {
            LinearizationDefinition definition = gedDefinitionByView(specification.getLinearizationView(), definitions);
            EntityPostCoordinationSpecificationDto dto = new EntityPostCoordinationSpecificationDto(definition.getId(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
            if(isMainAxis(definition)) {
                mapAsPrimary(specification, dto);
            } else  {
                mapAsTelescopic(specification, dto);
            }
            resp.add(dto);
        }

        return resp;
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

    private static LinearizationDefinition gedDefinitionByView(String linearizationView, List<LinearizationDefinition> definitions) {
        return definitions.stream().filter(def -> def.getWhoficEntityIri().equalsIgnoreCase(linearizationView))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error finding definition with id " + linearizationView));
    }
}
