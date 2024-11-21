package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

public record OWLEntityDto(String entityIRI,

                           EntityLanguageTerms languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           @JsonIgnore
                           Date lastChangeDate,

                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
