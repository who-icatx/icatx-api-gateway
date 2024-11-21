package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(AddEntitySpecificationRevisionRequest.CHANNEL)
public record AddEntitySpecificationRevisionRequest(@JsonProperty("projectId")
                                                    ProjectId projectId,
                                                    @JsonProperty("entitySpecification")
                                                    WhoficEntityPostCoordinationSpecification entitySpecification) implements Request<AddEntitySpecificationRevisionResponse> {

    public final static String CHANNEL = "webprotege.postcoordination.AddEntitySpecificationRevision";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
