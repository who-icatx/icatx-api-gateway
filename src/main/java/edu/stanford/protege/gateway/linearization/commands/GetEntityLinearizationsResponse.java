package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.time.LocalDateTime;
import java.util.Date;

import static edu.stanford.protege.gateway.linearization.commands.GetEntityLinearizationsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityLinearizationsResponse(@JsonProperty("entityIri")
                                              String entityIri,
                                              @JsonProperty("lastRevisionDate")
                                              Date lastRevisionDate,
                                              @JsonProperty("linearizationSpecification")
                                              WhoficEntityLinearizationSpecification linearizationSpecification) implements Response {
}
