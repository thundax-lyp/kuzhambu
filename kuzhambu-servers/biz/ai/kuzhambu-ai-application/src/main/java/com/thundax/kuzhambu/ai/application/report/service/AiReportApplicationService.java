package com.thundax.kuzhambu.ai.application.report.service;

import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import java.util.Date;

public interface AiReportApplicationService {

    @LayerPublicApi(reason = "Operations 报表按统一 summary 规格读取 AI 调用统计的跨模块入口")
    AiReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);
}
