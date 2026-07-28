package com.thundax.kuzhambu.operations.application.report.support;

import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class OperationsReportSupportModels {

    private OperationsReportSupportModels() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationsReportSnapshot {

        private Long reportId;
        private String requestId;
        private String traceId;
        private String reportType;
        private String format;
        private Date periodStart;
        private Date periodEnd;
        private String templateVersion;
        private Long requesterUserId;
        private Date generatedAt;
        private List<OperationsReportSection> sections;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationsReportSection {

        private String sectionKey;
        private String sectionTitle;
        private Map<String, Object> payload;
    }
}
