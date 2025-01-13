package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

public record OWLEntityDto(@JsonProperty("entityUri") String entityIRI,

                           EntityLanguageTerms languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           @JsonIgnore
                           LocalDateTime lastChangeDate,

                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
