package com.thundax.kuzhambu.operations.application.report.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSnapshot;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OperationsReportSnapshotAssembler {

    private static final String CALLER_DOMAIN = "OPERATIONS";
    private static final String OPERATION = "OPERATIONS_REPORT_GENERATE";
    private static final String RENDER_TYPE = "OPERATIONS_REPORT";
    private static final String CONTENT_TYPE = "OPERATIONS_REPORT_SNAPSHOT";
    private static final String LOCALE = "zh-CN";

    private final OperationsReportMetricsGateway operationsReportMetricsGateway;
    private final ObjectMapper objectMapper;

    public OperationsReportSnapshotAssembler(
            OperationsReportMetricsGateway operationsReportMetricsGateway, ObjectMapper objectMapper) {
        this.operationsReportMetricsGateway = operationsReportMetricsGateway;
        this.objectMapper = objectMapper;
    }

    public OperationsReportSnapshot assemble(ReportRecord record) {
        if (record == null) {
            return null;
        }
        List<OperationsReportSection> sections = new java.util.ArrayList<>();
        sections.add(new OperationsReportSection(
                "reportMeta",
                "报表任务元信息",
                Map.of(
                        "reportType", record.getReportType(),
                        "format", record.getFormat(),
                        "requesterUserId", record.getRequesterUserId(),
                        "periodStart", record.getPeriodStart(),
                        "periodEnd", record.getPeriodEnd())));
        sections.addAll(operationsReportMetricsGateway.loadSections(record));
        return new OperationsReportSnapshot(
                record.getId() == null ? null : record.getId().value(),
                record.getRequestId(),
                record.getTraceId(),
                record.getReportType(),
                record.getFormat(),
                record.getPeriodStart(),
                record.getPeriodEnd(),
                record.getTemplateVersion(),
                record.getRequesterUserId(),
                Instant.now(),
                sections);
    }

    public OperationsWorkerRenderDtos.WorkerRenderRequest toWorkerRequest(
            ReportRecord record, OperationsReportSnapshot snapshot) {
        if (record == null || snapshot == null) {
            return null;
        }

        OperationsWorkerRenderDtos.WorkerRenderRequest request = new OperationsWorkerRenderDtos.WorkerRenderRequest();
        request.setRequestId(record.getRequestId());
        request.setTraceId(record.getTraceId());
        request.setCallerDomain(CALLER_DOMAIN);
        request.setOperation(OPERATION);
        request.setRenderType(RENDER_TYPE);

        OperationsWorkerRenderDtos.Template template = new OperationsWorkerRenderDtos.Template();
        template.setTemplateId(templateId(record));
        template.setTemplateVersion(record.getTemplateVersion());
        request.setTemplate(template);

        OperationsWorkerRenderDtos.Output output = new OperationsWorkerRenderDtos.Output();
        output.setFormat(record.getFormat());
        output.setFilenameHint(filenameHint(record));
        output.setLocale(LOCALE);
        request.setOutput(output);

        OperationsWorkerRenderDtos.Input input = new OperationsWorkerRenderDtos.Input();
        input.setSnapshotId(
                record.getId() == null ? null : String.valueOf(record.getId().value()));
        input.setContentType(CONTENT_TYPE);
        input.setPayloadJson(toPayloadJson(snapshot));
        request.setInput(input);

        OperationsWorkerRenderDtos.Options options = new OperationsWorkerRenderDtos.Options();
        options.setStream(false);
        options.setIncludeMetadata(true);
        request.setOptions(options);
        return request;
    }

    private String templateId(ReportRecord record) {
        return "operations-report-" + lower(record.getReportType()) + "-" + lower(record.getFormat());
    }

    private String filenameHint(ReportRecord record) {
        Long reportId = record.getId() == null ? null : record.getId().value();
        return "operations-" + lower(record.getReportType()) + "-" + reportId + "." + lower(record.getFormat());
    }

    private String toPayloadJson(OperationsReportSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize operations report snapshot.", exception);
        }
    }

    private String lower(String value) {
        return value == null ? "unknown" : value.toLowerCase();
    }
}
