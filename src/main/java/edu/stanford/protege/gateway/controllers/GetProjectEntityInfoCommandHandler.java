package edu.stanford.protege.gateway.controllers;


import edu.stanford.protege.gateway.GetProjectEntityInfoRequest;
import edu.stanford.protege.gateway.GetProjectEntityInfoResponse;
import edu.stanford.protege.gateway.OwlEntityService;
import edu.stanford.protege.gateway.SecurityContextHelper;
import edu.stanford.protege.gateway.dto.OWLEntityDto;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@WebProtegeHandler
public class GetProjectEntityInfoCommandHandler implements CommandHandler<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GetProjectEntityInfoCommandHandler.class);

    private final OwlEntityService owlEntityService;

    public GetProjectEntityInfoCommandHandler(OwlEntityService owlEntityService) {
        this.owlEntityService = owlEntityService;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetProjectEntityInfoRequest.CHANNEL;
    }

    @Override
    public Class<GetProjectEntityInfoRequest> getRequestClass() {
        return GetProjectEntityInfoRequest.class;
    }

    @Override
    public Mono<GetProjectEntityInfoResponse> handleRequest(GetProjectEntityInfoRequest request, ExecutionContext executionContext) {

        OWLEntityDto dto = owlEntityService.getEntityInfo(request.entityIri().toString(), request.projectId().id(), executionContext);


        return Mono.just(new GetProjectEntityInfoResponse(dto));
    }
}
