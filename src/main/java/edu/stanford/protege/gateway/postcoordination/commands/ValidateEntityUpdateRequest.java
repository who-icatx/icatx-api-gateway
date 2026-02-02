package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(ValidateEntityUpdateRequest.CHANNEL)
public record ValidateEntityUpdateRequest(@JsonProperty("projectId")
                                         ProjectId projectId,
                                         @JsonProperty("entityCustomScaleValues")
                                         WhoficCustomScalesValues entityCustomScaleValues,
                                         @JsonProperty("entitySpecification")
                                         WhoficEntityPostCoordinationSpecification entitySpecification) implements Request<ValidateEntityUpdateResponse> {

    public final static String CHANNEL = "webprotege.postcoordination.ValidateEntityUpdate";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
