package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.ProjectId;

import java.util.List;

public record EntityCommentThread(@JsonProperty("projectId") ProjectId projectId,
                                  @JsonProperty("entityIRI") String entityIRI,
                                  @JsonProperty("status") String status,
                                  @JsonProperty("comments") List<EntityComment> entityComments) {

}
