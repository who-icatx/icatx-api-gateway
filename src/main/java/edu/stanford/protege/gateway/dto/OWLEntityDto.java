package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({ "entityUri", "languageTerms", "entityLinearizations", "postcoordination", "logicalConditions", "parents"})
public record OWLEntityDto(@JsonProperty("entityUri") String entityIRI,

                           boolean isObsolete,
                           EntityLanguageTermsDto languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           @JsonIgnore
                           LocalDateTime lastChangeDate,

                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
