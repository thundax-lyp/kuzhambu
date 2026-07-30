package com.thundax.kuzhambu.classics.application.report.service;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import java.time.Instant;

public interface ClassicsReportApplicationService {

    @LayerPublicApi(reason = "Operations 报表按统一 summary 规格读取 Classics 聚合统计的跨模块入口")
    ClassicsReportSummaryResult summary(Instant periodStart, Instant periodEnd, String bucketType);
}
