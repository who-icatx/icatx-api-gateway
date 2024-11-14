package edu.stanford.protege.gateway.dto;

import edu.stanford.protege.webprotege.common.UserId;

import java.time.LocalDateTime;

public record EntityChange(String changeSummary,
                           UserId userId,
                           LocalDateTime dateTime) {
}
