package com.thundax.kuzhambu.discovery.application.facade.impl;

import com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoverySearchPublicationFacadeAssembler;
import com.thundax.kuzhambu.discovery.application.search.service.SearchPublicationApplicationService;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;
import org.springframework.stereotype.Service;

@Service
public class DiscoverySearchPublicationFacadeImpl implements DiscoverySearchPublicationFacade {

    private final SearchPublicationApplicationService searchPublicationApplicationService;
    private final DiscoverySearchPublicationFacadeAssembler assembler;

    public DiscoverySearchPublicationFacadeImpl(
            SearchPublicationApplicationService searchPublicationApplicationService,
            DiscoverySearchPublicationFacadeAssembler assembler) {
        this.searchPublicationApplicationService = searchPublicationApplicationService;
        this.assembler = assembler;
    }

    @Override
    public void prepare(DiscoverySearchPublicationPrepareFacadeRequest request) {
        searchPublicationApplicationService.prepare(assembler.toPrepareCommand(request));
    }

    @Override
    public void markReady(DiscoverySearchPublicationReferenceFacadeRequest request) {
        searchPublicationApplicationService.markReady(assembler.toReferenceCommand(request));
    }

    @Override
    public void markOffline(DiscoverySearchPublicationReferenceFacadeRequest request) {
        searchPublicationApplicationService.markOffline(assembler.toReferenceCommand(request));
    }

    @Override
    public void delete(DiscoverySearchPublicationReferenceFacadeRequest request) {
        searchPublicationApplicationService.delete(assembler.toReferenceCommand(request));
    }

    @Override
    public DiscoverySearchPublicationProbeFacadeResponse probe(
            DiscoverySearchPublicationReferenceFacadeRequest request) {
        return assembler.toProbeResponse(
                searchPublicationApplicationService.probe(assembler.toReferenceCommand(request)));
    }
}
