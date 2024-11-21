package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(AddEntityCustomScalesRevisionRequest.CHANNEL)
public record AddEntityCustomScalesRevisionRequest(@JsonProperty("projectId")
                                                   ProjectId projectId,
                                                   @JsonProperty("entityCustomScaleValues")
                                                   WhoficCustomScalesValues entityCustomScaleValues) implements Request<AddEntityCustomScalesRevisionResponse> {

    public final static String CHANNEL = "webprotege.postcoordination.AddEntityCustomScalesRevision";

    @JsonCreator
    public static AddEntityCustomScalesRevisionRequest create(@JsonProperty("projectId")
                                                              ProjectId projectId,
                                                              @JsonProperty("entityCustomScaleValues")
                                                              WhoficCustomScalesValues entityCustomScaleValues) {
        return new AddEntityCustomScalesRevisionRequest(projectId, entityCustomScaleValues);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }


}
