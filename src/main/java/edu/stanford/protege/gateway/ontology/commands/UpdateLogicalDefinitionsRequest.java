package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.OWLClass;

@JsonTypeName(UpdateLogicalDefinitionsRequest.CHANNEL)
public record UpdateLogicalDefinitionsRequest(
        @JsonProperty("changeRequestId") ChangeRequestId changeRequestId,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("subject") OWLClass subject,
        @JsonProperty("pristineLogicalConditions") OntologicalLogicalDefinitionConditions pristineLogicalConditions,
        @JsonProperty("changedLogicalConditions") OntologicalLogicalDefinitionConditions changedLogicalConditions,
        @JsonProperty("commitMessage") String commitMessage
) implements Request<UpdateLogicalDefinitionsResponse> {


    public final static String CHANNEL = "icatx.logicalDefinitions.UpdateLogicalDefinition";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static UpdateLogicalDefinitionsRequest create(ChangeRequestId changeRequestId,
                                                         ProjectId projectId,
                                                         OWLClass subject,
                                                         OntologicalLogicalDefinitionConditions pristineLogicalConditions,
                                                         OntologicalLogicalDefinitionConditions changedLogicalConditions,
                                                         String commitMessage) {
        return new UpdateLogicalDefinitionsRequest(changeRequestId, projectId, subject,
                pristineLogicalConditions, changedLogicalConditions, commitMessage);
    }
}
