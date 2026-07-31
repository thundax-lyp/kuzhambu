package com.thundax.kuzhambu.classics.application.publication.result;

import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import java.util.List;

public record ClassicsPublicationPayload(
        DiscoverySearchPublicationPrepareFacadeRequest searchDocument,
        String fastGptCollectionName,
        List<ClassicsPublicationFragment> fastGptFragments) {}
