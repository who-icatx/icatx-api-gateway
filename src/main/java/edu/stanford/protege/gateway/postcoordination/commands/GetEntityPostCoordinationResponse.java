package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.util.Date;

import static edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValuesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityPostCoordinationResponse(@JsonProperty("entityIri")
                                                String entityIri,
                                                @JsonProperty("lastRevisionDate")
                                                Date lastRevisionDate,
                                                @JsonProperty("postCoordinationSpecification")
                                                WhoficEntityPostCoordinationSpecification postCoordinationSpecification) implements Response {
}
