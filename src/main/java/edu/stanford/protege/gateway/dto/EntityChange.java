package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.UserId;

import java.time.LocalDateTime;

public record EntityChange(String changeSummary,
                           UserId userId,
                           @JsonProperty("timestamp") LocalDateTime timestamp) {
}
