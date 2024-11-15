package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.UserId;

import java.time.*;

public record EntityComment(@JsonProperty("createdBy") UserId userId,
                            @JsonProperty("createdAt") ZonedDateTime createdAt,
                            @JsonProperty("updatedAt") ZonedDateTime updatedAt,
                            @JsonProperty("body") String body) {

}
