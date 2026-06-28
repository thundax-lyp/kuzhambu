package com.thundax.kuzhambu.discovery.application.facade.impl;

import com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoveryFacadeAssembler;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.discovery.facade.DiscoveryFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySummaryFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscoveryFacadeImpl implements DiscoveryFacade {

    private final DiscoveryReportApplicationService discoveryReportApplicationService;
    private final DiscoveryFacadeAssembler discoveryFacadeAssembler;

    public DiscoveryFacadeImpl(
            DiscoveryReportApplicationService discoveryReportApplicationService,
            DiscoveryFacadeAssembler discoveryFacadeAssembler) {
        this.discoveryReportApplicationService = discoveryReportApplicationService;
        this.discoveryFacadeAssembler = discoveryFacadeAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public DiscoverySummaryFacadeResponse summary(DiscoverySummaryFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return discoveryFacadeAssembler.toSummaryFacadeResponse(discoveryReportApplicationService.summary(
                request.getPeriodStart(), request.getPeriodEnd(), request.getBucketType()));
    }
}
