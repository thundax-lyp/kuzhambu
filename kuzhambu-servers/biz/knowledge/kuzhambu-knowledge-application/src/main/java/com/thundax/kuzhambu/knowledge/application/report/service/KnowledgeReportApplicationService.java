package com.thundax.kuzhambu.knowledge.application.report.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import java.time.Instant;

public interface KnowledgeReportApplicationService {

    @LayerPublicApi(reason = "Operations 报表按统一 summary 规格读取 Knowledge 标签治理统计的跨模块入口")
    KnowledgeReportSummaryResult summary(Instant periodStart, Instant periodEnd, String bucketType);
}
