package com.thundax.kuzhambu.discovery.facade;

import com.thundax.kuzhambu.discovery.facade.request.DiscoverySummaryFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;

public interface DiscoveryFacade {

    DiscoverySummaryFacadeResponse summary(DiscoverySummaryFacadeRequest request);
}
