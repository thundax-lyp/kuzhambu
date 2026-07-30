package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionSnapshot;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryGateway;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DefaultOperationsReportMetricsGateway implements OperationsReportMetricsGateway {

    private final OperationsDashboardSummaryGateway summaryGateway;

    public DefaultOperationsReportMetricsGateway(OperationsDashboardSummaryGateway summaryGateway) {
        this.summaryGateway = summaryGateway;
    }

    @Override
    public List<OperationsReportSection> loadSections(ReportRecord record) {
        if (record == null) {
            return List.of();
        }
        String bucketType = resolveBucketType(record.getReportType());
        OperationsCrossDomainSummary summary = summaryGateway.loadSummary(
                record.getPeriodStart(),
                record.getPeriodEnd(),
                bucketType,
                new OperationsDashboardPermissionSnapshot(true, true, true, true, true, true, true, true));
        List<OperationsReportSection> sections = new ArrayList<>();
        sections.add(section("classicsSummary", "Classics 统计摘要", summary.classicsSummary()));
        sections.add(section("aiSummary", "AI 调用摘要", summary.aiSummary()));
        sections.add(section("discoverySummary", "Discovery 统计摘要", summary.discoverySummary()));
        sections.add(section("knowledgeSummary", "Knowledge 统计摘要", summary.knowledgeSummary()));
        return sections;
    }

    private OperationsReportSection section(String key, String title, Object summary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", summary);
        return new OperationsReportSection(key, title, payload);
    }

    private String resolveBucketType(String reportType) {
        return StringUtils.containsIgnoreCase(reportType, "MONTH") ? "WEEK" : "DAY";
    }
}
