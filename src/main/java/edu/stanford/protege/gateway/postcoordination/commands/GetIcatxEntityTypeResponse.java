package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;


@JsonTypeName(GetIcatxEntityTypeRequest.CHANNEL)
public record GetIcatxEntityTypeResponse(@JsonProperty("icatxEntityTypes") List<String> icatxEntityTypes) implements Response {
}
