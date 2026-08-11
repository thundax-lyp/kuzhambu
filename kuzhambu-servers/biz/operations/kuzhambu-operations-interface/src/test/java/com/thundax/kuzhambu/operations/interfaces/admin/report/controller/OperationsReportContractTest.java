package com.thundax.kuzhambu.operations.interfaces.admin.report.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportGenerateResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportPageResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OperationsReportContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void requestJsonFieldsShouldRemainStable() throws Exception {
        OperationsReportGenerateRequest generateRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "reportType":"WEEKLY",
                  "format":"PDF",
                  "periodStart":1717171200000,
                  "periodEnd":1717775999000
                }
                """,
                OperationsReportGenerateRequest.class);
        assertEquals("WEEKLY", generateRequest.getReportType());
        assertEquals("PDF", generateRequest.getFormat());
        assertEquals(Instant.ofEpochMilli(1_717_171_200_000L), generateRequest.getPeriodStart());
        assertEquals(Instant.ofEpochMilli(1_717_775_999_000L), generateRequest.getPeriodEnd());
        assertJsonFields(generateRequest, "reportType", "format", "periodStart", "periodEnd");

        OperationsReportPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "reportType":"MONTHLY",
                  "format":"HTML",
                  "reportStatus":"SUCCEEDED",
                  "requesterUserId":1001,
                  "periodStart":"2024-06-01T00:00:00.000+08:00",
                  "periodEnd":"2024-06-30T23:59:59.000+08:00",
                  "pageNo":1,
                  "pageSize":20
                }
                """,
                OperationsReportPageRequest.class);
        assertEquals("MONTHLY", pageRequest.getReportType());
        assertEquals("SUCCEEDED", pageRequest.getReportStatus());
        assertEquals(1, pageRequest.getPageNo());
        assertEquals(Instant.parse("2024-05-31T16:00:00Z"), pageRequest.getPeriodStart());
        assertEquals(Instant.parse("2024-06-30T15:59:59Z"), pageRequest.getPeriodEnd());
        assertJsonFields(
                pageRequest,
                "reportType",
                "format",
                "reportStatus",
                "requesterUserId",
                "periodStart",
                "periodEnd",
                "pageNo",
                "pageSize");

        OperationsReportDetailRequest detailRequest = OBJECT_MAPPER.readValue(
                """
                {"reportId":9001}
                """, OperationsReportDetailRequest.class);
        assertEquals(9001L, detailRequest.getReportId());
        assertJsonFields(detailRequest, "reportId");
    }

    @Test
    void responseJsonFieldsShouldRemainStable() throws Exception {
        OperationsReportGenerateResponse generateResponse = OperationsReportGenerateResponse.builder()
                .reportId(9001L)
                .reportStatus("PENDING")
                .build();
        assertJsonFields(generateResponse, "reportId", "reportStatus");

        OperationsReportPageResponse pageResponse = OperationsReportPageResponse.builder()
                .reportId(9001L)
                .reportType("WEEKLY")
                .format("PDF")
                .periodStart(Instant.ofEpochMilli(1_718_000_000_000L))
                .periodEnd(Instant.ofEpochMilli(1_718_086_400_000L))
                .storageObjectId(3001L)
                .artifactFilename("weekly-report.pdf")
                .reportStatus("SUCCEEDED")
                .failureReason("failed")
                .requesterUserId(1001L)
                .requestedAt(Instant.ofEpochMilli(1_718_086_500_000L))
                .completedAt(Instant.ofEpochMilli(1_718_086_600_000L))
                .build();
        assertJsonFields(
                pageResponse,
                "reportId",
                "reportType",
                "format",
                "periodStart",
                "periodEnd",
                "storageObjectId",
                "artifactFilename",
                "reportStatus",
                "failureReason",
                "requesterUserId",
                "requestedAt",
                "completedAt");

        OperationsReportDetailResponse detailResponse = OperationsReportDetailResponse.builder()
                .reportId(9001L)
                .reportType("WEEKLY")
                .format("PDF")
                .periodStart(Instant.ofEpochMilli(1_718_000_000_000L))
                .periodEnd(Instant.ofEpochMilli(1_718_086_400_000L))
                .requestId("req-1")
                .traceId("trace-1")
                .templateVersion("2026.06.26")
                .storageObjectId(3001L)
                .artifactFilename("weekly-report.pdf")
                .reportStatus("SUCCEEDED")
                .failureReason("failed")
                .requesterUserId(1001L)
                .requestedAt(Instant.ofEpochMilli(1_718_086_500_000L))
                .completedAt(Instant.ofEpochMilli(1_718_086_600_000L))
                .build();
        assertJsonFields(
                detailResponse,
                "reportId",
                "reportType",
                "format",
                "periodStart",
                "periodEnd",
                "requestId",
                "traceId",
                "templateVersion",
                "storageObjectId",
                "artifactFilename",
                "reportStatus",
                "failureReason",
                "requesterUserId",
                "requestedAt",
                "completedAt");
    }

    private void assertJsonFields(Object value, String... fieldNames) {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
