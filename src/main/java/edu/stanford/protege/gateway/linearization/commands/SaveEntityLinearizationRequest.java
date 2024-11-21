package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import jakarta.validation.constraints.NotNull;

@JsonTypeName(SaveEntityLinearizationRequest.CHANNEL)
public record SaveEntityLinearizationRequest(
        @JsonProperty("projectId")
        ProjectId projectId,
        @JsonProperty("entityLinearization")
        WhoficEntityLinearizationSpecification entityLinearization,
        @JsonProperty("changeRequestId") @NotNull ChangeRequestId changeRequestId
) implements Request<SaveEntityLinearizationResponse> {

    public static final String CHANNEL = "webprotege.linearization.SaveEntityLinearization";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
