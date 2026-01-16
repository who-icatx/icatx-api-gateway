package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;


@JsonTypeName(GetFullPostCoordinationConfigurationRequest.CHANNEL)
public class GetFullPostCoordinationConfigurationRequest implements Request<GetFullPostCoordinationConfigurationResponse> {

    public final static String CHANNEL = "webprotege.postcoordination.GetFullPostCoordinationConfiguration";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
