package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import jakarta.validation.constraints.NotNull;

@JsonTypeName(AddEntityCustomScalesRevisionRequest.CHANNEL)
public record AddEntityCustomScalesRevisionRequest(@JsonProperty("projectId")
                                                   ProjectId projectId,
                                                   @JsonProperty("entityCustomScaleValues")
                                                   WhoficCustomScalesValues entityCustomScaleValues,
                                                   @JsonProperty("changeRequestId") @NotNull ChangeRequestId changeRequestId
) implements Request<AddEntityCustomScalesRevisionResponse> {

    public final static String CHANNEL = "webprotege.postcoordination.AddEntityCustomScalesRevision";

    @Override
    public String getChannel() {
        return CHANNEL;
    }


}
