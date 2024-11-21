package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.protege.webprotege.common.ChangeRequest;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.ProjectRequest;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

public record SetEntityFormDataFromJsonRequest(@Nonnull ChangeRequestId changeRequestId,
                                               @Nonnull ProjectId projectId,
                                               @Nonnull OWLEntity owlEntity,
                                               @Nonnull String formId,
                                               @Nonnull JsonNode jsonFormData) implements ProjectRequest<SetEntityFormDataFromJsonResponse>, ChangeRequest {


    public static final String CHANNEL = "webprotege.forms.SetEntityFormFromJson";

    @Override
    public ChangeRequestId changeRequestId() {
        return changeRequestId;
    }

    @Nonnull
    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
