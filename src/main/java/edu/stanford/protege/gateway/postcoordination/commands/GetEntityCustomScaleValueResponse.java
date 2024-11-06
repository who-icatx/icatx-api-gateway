package edu.stanford.protege.gateway.postcoordination.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import java.util.Date;

import static edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValuesRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityCustomScaleValueResponse(
        @JsonProperty("whoficCustomScaleValues") WhoficCustomScalesValues whoficCustomScalesValues,
        @JsonProperty("lastRevisionDate")
        Date lastRevisionDate
) implements Response {
}
