package edu.stanford.protege.gateway.postcoordination;

import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValueResponse;
import edu.stanford.protege.gateway.postcoordination.commands.PostCoordinationScaleCustomization;
import edu.stanford.protege.gateway.postcoordination.commands.WhoficCustomScalesValues;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomScalesMapper {


    public static List<EntityPostCoordinationCustomScalesDto> mapFromResponse(GetEntityCustomScaleValueResponse response) {
        return response.whoficCustomScalesValues().scaleCustomizations().stream()
                .map(scale -> new EntityPostCoordinationCustomScalesDto(new ArrayList<>(scale.getPostcoordinationScaleValues()), scale.getPostcoordinationAxis()))
                .collect(Collectors.toList());
    }

    public static WhoficCustomScalesValues mapFromDtoList(String entityIri, List<EntityPostCoordinationCustomScalesDto> dtos) {

        List<PostCoordinationScaleCustomization> scaleCustomizations = dtos.stream()
                .map(dto -> new PostCoordinationScaleCustomization(dto.postcoordinationScaleValues(), dto.postcoordinationAxis()))
                .toList();

        return new WhoficCustomScalesValues(entityIri, scaleCustomizations);
    }


}
