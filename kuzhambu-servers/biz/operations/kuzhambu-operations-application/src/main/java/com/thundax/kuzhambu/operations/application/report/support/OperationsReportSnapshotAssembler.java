package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSnapshot;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class OperationsReportSnapshotAssembler {

    private static final String CALLER_DOMAIN = "OPERATIONS";
    private static final String OPERATION = "OPERATIONS_REPORT_GENERATE";
    private static final String RENDER_TYPE = "OPERATIONS_REPORT";
    private static final String CONTENT_TYPE = "OPERATIONS_REPORT_SNAPSHOT";
    private static final String LOCALE = "zh-CN";

    public OperationsReportSnapshot assemble(ReportRecord record) {
        if (record == null) {
            return null;
        }
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
                new Date(),
                List.of(new OperationsReportSection(
                        "reportMeta",
                        "报表任务元信息",
                        Map.of(
                                "reportType", record.getReportType(),
                                "format", record.getFormat(),
                                "requesterUserId", record.getRequesterUserId(),
                                "periodStart", record.getPeriodStart(),
                                "periodEnd", record.getPeriodEnd()))));
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
        StringJoiner sectionsJoiner = new StringJoiner(",", "[", "]");
        if (snapshot.getSections() != null) {
            for (OperationsReportSection section : snapshot.getSections()) {
                sectionsJoiner.add("{"
                        + "\"sectionKey\":\"" + json(section.getSectionKey()) + "\","
                        + "\"sectionTitle\":\"" + json(section.getSectionTitle()) + "\","
                        + "\"payload\":" + toPayloadJson(section.getPayload())
                        + "}");
            }
        }
        return "{"
                + "\"reportId\":" + number(snapshot.getReportId()) + ","
                + "\"requestId\":\"" + json(snapshot.getRequestId()) + "\","
                + "\"traceId\":\"" + json(snapshot.getTraceId()) + "\","
                + "\"reportType\":\"" + json(snapshot.getReportType()) + "\","
                + "\"format\":\"" + json(snapshot.getFormat()) + "\","
                + "\"periodStart\":\"" + json(snapshot.getPeriodStart()) + "\","
                + "\"periodEnd\":\"" + json(snapshot.getPeriodEnd()) + "\","
                + "\"templateVersion\":\"" + json(snapshot.getTemplateVersion()) + "\","
                + "\"requesterUserId\":" + number(snapshot.getRequesterUserId()) + ","
                + "\"generatedAt\":\"" + json(snapshot.getGeneratedAt()) + "\","
                + "\"sections\":" + sectionsJoiner
                + "}";
    }

    private String lower(String value) {
        return value == null ? "unknown" : value.toLowerCase();
    }

    private String toPayloadJson(Map<String, Object> payload) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            joiner.add("\"" + json(entry.getKey()) + "\":" + value(entry.getValue()));
        }
        return joiner.toString();
    }

    private String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + json(value) + "\"";
    }

    private String number(Number value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String json(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
