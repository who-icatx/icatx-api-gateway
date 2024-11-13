package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.hierarchy.*;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.gateway.ontology.commands.GetEntityChildrenRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityChildrenRequest(
        @JsonProperty("classIri") IRI classIri,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("hierarchyDescriptor") HierarchyDescriptor hierarchyDescriptor
) implements Request<GetEntityChildrenResponse> {
    public static final String CHANNEL = "webprotege.entities.GetEntityChildren";


    public static GetEntityChildrenRequest create(IRI classIri,
                                                  ProjectId projectId) {
        return new GetEntityChildrenRequest(classIri, projectId, ClassHierarchyDescriptor.create());
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
