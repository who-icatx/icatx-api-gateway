package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({"entityUri", "lastModified", "languageTerms", "entityLinearizations", "postcoordination", "logicalConditions", "parents"})
public record OWLEntityDto(@JsonProperty("entityUri") String entityIRI,
                           @JsonProperty("lastModified") LocalDateTime lastChangeDate,
                           EntityLanguageTerms languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
