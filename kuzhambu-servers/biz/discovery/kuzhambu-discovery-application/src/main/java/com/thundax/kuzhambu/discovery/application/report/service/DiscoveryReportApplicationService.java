package com.thundax.kuzhambu.discovery.application.report.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import java.util.Date;

public interface DiscoveryReportApplicationService {

    @LayerPublicApi(reason = "Operations 报表按统一 summary 规格读取 Discovery 聚合统计的跨模块入口")
    DiscoveryReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType);
}
