package edu.stanford.protege.gateway.postcoordination.commands;


import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.postcoordination.commands.AddEntityCustomScalesRevisionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public class AddEntityCustomScalesRevisionResponse implements Response {
}
