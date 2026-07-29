package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.application.invocation.query.AiReportSummaryQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;

public interface AiReportApplicationService {

    @LayerPublicApi(reason = "Operations 报表按统一 summary 规格读取 AI 调用统计的跨模块入口")
    AiReportSummaryResult summary(AiReportSummaryQuery query);
}
