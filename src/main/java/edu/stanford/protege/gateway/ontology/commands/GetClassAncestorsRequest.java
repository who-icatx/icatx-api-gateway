package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.gateway.ontology.commands.GetClassAncestorsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public class GetClassAncestorsRequest implements Request<GetClassAncestorsResponse> {
    public static final String CHANNEL = "webprotege.entities.GetClassAncestors";

    private final IRI classIri;

    private final ProjectId projectId;


    @JsonCreator
    public GetClassAncestorsRequest(@JsonProperty("classIri") IRI classIri,@JsonProperty("projectId") ProjectId projectId) {
        this.classIri = classIri;
        this.projectId = projectId;
    }

    @JsonProperty("classIri")
    public IRI getClassIri() {
        return classIri;
    }
    @JsonProperty("projectId")
    public ProjectId getProjectId() {
        return projectId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
