package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.gateway.postcoordination.commands.AddEntitySpecificationRevisionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public class AddEntitySpecificationRevisionResponse implements Response {
}
