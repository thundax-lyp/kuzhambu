package com.thundax.kuzhambu.ai.facade;

import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;

public interface AiFacade {

    AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request);

    DiscoveryAiFacadeResponse understandDiscoveryQuery(DiscoveryAiFacadeRequest request);

    DiscoveryAiFacadeResponse generateDiscoveryAnswer(DiscoveryAiFacadeRequest request);
}
