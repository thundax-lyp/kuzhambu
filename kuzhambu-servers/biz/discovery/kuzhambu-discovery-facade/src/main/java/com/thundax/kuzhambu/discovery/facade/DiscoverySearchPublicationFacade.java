package com.thundax.kuzhambu.discovery.facade;

import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;

public interface DiscoverySearchPublicationFacade {

    void prepare(DiscoverySearchPublicationPrepareFacadeRequest request);

    void markReady(DiscoverySearchPublicationReferenceFacadeRequest request);

    void markOffline(DiscoverySearchPublicationReferenceFacadeRequest request);

    void delete(DiscoverySearchPublicationReferenceFacadeRequest request);

    DiscoverySearchPublicationProbeFacadeResponse probe(DiscoverySearchPublicationReferenceFacadeRequest request);
}
