package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import java.util.Set;

import static edu.stanford.protege.gateway.ontology.commands.CreateClassesFromApiRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateClassesFromApiResponse(@JsonProperty("changeRequestId") ChangeRequestId changeRequestId, ProjectId projectId,
                                           @JsonProperty("newEntityIris") Set<String> newEntityIris) implements Response {
}
