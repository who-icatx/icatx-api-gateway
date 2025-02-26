package edu.stanford.protege.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.common.ProjectId;

import javax.annotation.Nonnull;

public record EntityUpdatedSuccessfullyEvent(@JsonProperty("projectId") ProjectId projectId,
                                             @JsonProperty("eventId") EventId eventId,

                                             @JsonProperty("entityIri") String entityIri,
                                             @JsonProperty("changeRequestId") ChangeRequestId changeRequestId) implements ProjectEvent {

    public static String CHANNEL = "webprotege.api.EntityUpdatedSuccessfully";


    @Nonnull
    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @Nonnull
    @Override
    public EventId eventId() {
        return eventId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
