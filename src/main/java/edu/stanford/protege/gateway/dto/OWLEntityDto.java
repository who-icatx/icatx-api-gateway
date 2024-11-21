package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record OWLEntityDto(String entityURI,

                           EntityLanguageTerms languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           @JsonIgnore
                           LocalDateTime lastChangeDate,

                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
