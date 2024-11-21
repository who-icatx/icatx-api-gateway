package edu.stanford.protege.gateway.history.commands;

import edu.stanford.protege.webprotege.common.UserId;

import java.time.LocalDateTime;

public record EntityChange(String changeSummary,
                           UserId userId,
                           LocalDateTime dateTime) {

    public static EntityChange create(String changeSummary,
                                      UserId userId,
                                      LocalDateTime dateTime){
        return new EntityChange(changeSummary,userId,dateTime);
    }
}
