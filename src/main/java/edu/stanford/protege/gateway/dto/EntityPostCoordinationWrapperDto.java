package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

public record EntityPostCoordinationWrapperDto(
        List<EntityPostCoordinationSpecificationDto> postcoordinationSpecifications,

        @JsonIgnore
        Date lastRevisionDate,
        List<EntityPostCoordinationCustomScalesDto> scaleCustomizations) {

}
