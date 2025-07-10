package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({"entityUri", "isObsolete", "lastModified", "languageTerms", "diagnosticCriteria", "entityLinearizations", "postcoordination", "logicalConditions", "parents"})
public record OWLEntityDto(@JsonProperty("entityUri") String entityIRI,
                           boolean isObsolete,
                           EntityLanguageTermsDto languageTerms,
                           @JsonInclude(JsonInclude.Include.NON_NULL) String diagnosticCriteria,
                           @JsonProperty("lastModified") LocalDateTime lastChangeDate,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents,
                           @JsonProperty("relatedICFEntity") List<String> relatedIcfEntities) {

}
