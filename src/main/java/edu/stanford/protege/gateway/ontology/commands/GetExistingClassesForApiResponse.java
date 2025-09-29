package edu.stanford.protege.gateway.ontology.commands;

import edu.stanford.protege.webprotege.common.Page;

import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

public record GetExistingClassesForApiResponse(Page<ExistingClasses> existingClassesList) implements Response {


    public record ExistingClasses(String browserText, IRI iri){}
}
