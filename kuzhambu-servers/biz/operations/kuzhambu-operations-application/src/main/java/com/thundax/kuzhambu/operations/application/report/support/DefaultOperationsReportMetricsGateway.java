package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
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

    private final ClassicsFacade classicsFacade;
    private final AiFacade aiFacade;
    private final DiscoveryReportApplicationService discoveryReportApplicationService;
    private final KnowledgeFacade knowledgeFacade;

    public DefaultOperationsReportMetricsGateway(
            ClassicsFacade classicsFacade,
            AiFacade aiFacade,
            DiscoveryReportApplicationService discoveryReportApplicationService,
            KnowledgeFacade knowledgeFacade) {
        this.classicsFacade = classicsFacade;
        this.aiFacade = aiFacade;
        this.discoveryReportApplicationService = discoveryReportApplicationService;
        this.knowledgeFacade = knowledgeFacade;
    }

    @Override
    public List<OperationsReportSection> loadSections(ReportRecord record) {
        if (record == null) {
            return List.of();
        }
        String bucketType = resolveBucketType(record.getReportType());
        List<OperationsReportSection> sections = new ArrayList<>();
        sections.add(section(
                "classicsSummary",
                "Classics 统计摘要",
                classicsFacade.summary(ClassicsSummaryFacadeRequest.builder()
                        .periodStart(record.getPeriodStart())
                        .periodEnd(record.getPeriodEnd())
                        .bucketType(bucketType)
                        .build())));
        sections.add(section(
                "aiSummary",
                "AI 调用摘要",
                aiFacade.summary(AiReportSummaryFacadeRequest.builder()
                        .periodStart(record.getPeriodStart())
                        .periodEnd(record.getPeriodEnd())
                        .bucketType(bucketType)
                        .build())));
        sections.add(section(
                "discoverySummary",
                "Discovery 统计摘要",
                discoveryReportApplicationService.summary(record.getPeriodStart(), record.getPeriodEnd(), bucketType)));
        sections.add(section(
                "knowledgeSummary",
                "Knowledge 统计摘要",
                knowledgeFacade.summary(KnowledgeSummaryFacadeRequest.builder()
                        .periodStart(record.getPeriodStart())
                        .periodEnd(record.getPeriodEnd())
                        .bucketType(bucketType)
                        .build())));
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
