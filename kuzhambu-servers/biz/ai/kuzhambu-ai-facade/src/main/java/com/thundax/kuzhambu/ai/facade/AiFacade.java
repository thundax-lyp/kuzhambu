package com.thundax.kuzhambu.ai.facade;

import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;

public interface AiFacade {

    AiReportSummaryFacadeResponse summary(AiReportSummaryFacadeRequest request);
}
