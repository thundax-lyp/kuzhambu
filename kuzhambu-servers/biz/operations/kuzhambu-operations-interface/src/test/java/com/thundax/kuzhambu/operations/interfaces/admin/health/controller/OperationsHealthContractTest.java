package com.thundax.kuzhambu.operations.interfaces.admin.health.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertAckRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertRecoverRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertPageResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthPageResponse;
import java.util.Date;
import org.junit.jupiter.api.Test;

class OperationsHealthContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void healthPageRequestJsonFieldsShouldRemainStable() throws Exception {
        OperationsHealthPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "component":"admin-starter",
                  "healthStatus":"DOWN",
                  "probeSource":"HTTP",
                  "probeTarget":"internal/health",
                  "checkedAtStart":1719630400000,
                  "checkedAtEnd":1719716800000,
                  "pageNo":1,
                  "pageSize":20
                }
                """,
                OperationsHealthPageRequest.class);
        assertEquals("admin-starter", pageRequest.getComponent());
        assertEquals("DOWN", pageRequest.getHealthStatus());
        assertEquals("HTTP", pageRequest.getProbeSource());
        assertEquals("internal/health", pageRequest.getProbeTarget());
        assertEquals(new Date(1_719_630_400_000L), pageRequest.getCheckedAtStart());
        assertEquals(new Date(1_719_716_800_000L), pageRequest.getCheckedAtEnd());
        assertJsonFields(
                pageRequest,
                "component",
                "healthStatus",
                "probeSource",
                "probeTarget",
                "checkedAtStart",
                "checkedAtEnd",
                "pageNo",
                "pageSize");
    }

    @Test
    void healthPageResponseJsonFieldsShouldRemainStable() {
        OperationsHealthPageResponse pageResponse = OperationsHealthPageResponse.builder()
                .checkId(9101L)
                .component("admin-starter")
                .healthStatus("DOWN")
                .latencyMs(3000)
                .message("http probe timeout")
                .probeSource("HTTP")
                .probeTarget("http://127.0.0.1:8080/internal/health")
                .detailsJson("{\"errorType\":\"TIMEOUT\"}")
                .checkedAt(new Date(1_719_630_400_000L))
                .build();
        assertJsonFields(
                pageResponse,
                "checkId",
                "component",
                "healthStatus",
                "latencyMs",
                "message",
                "probeSource",
                "probeTarget",
                "detailsJson",
                "checkedAt");
    }

    @Test
    void alertRequestJsonFieldsShouldRemainStable() throws Exception {
        OperationsHealthAlertPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "component":"database",
                  "alertLevel":"CRITICAL",
                  "alertStatus":"ACTIVE",
                  "sourceRefType":"HEALTH",
                  "sourceRefId":9001,
                  "pageNo":1,
                  "pageSize":20
                }
                """,
                OperationsHealthAlertPageRequest.class);
        assertEquals("database", pageRequest.getComponent());
        assertEquals("CRITICAL", pageRequest.getAlertLevel());
        assertEquals("ACTIVE", pageRequest.getAlertStatus());
        assertEquals("HEALTH", pageRequest.getSourceRefType());
        assertEquals(9001L, pageRequest.getSourceRefId());
        assertJsonFields(
                pageRequest,
                "component",
                "alertLevel",
                "alertStatus",
                "sourceRefType",
                "sourceRefId",
                "pageNo",
                "pageSize");

        OperationsHealthAlertAckRequest ackRequest = OBJECT_MAPPER.readValue(
                """
                {"alertId":9201}
                """, OperationsHealthAlertAckRequest.class);
        assertEquals(9201L, ackRequest.getAlertId());
        assertJsonFields(ackRequest, "alertId");

        OperationsHealthAlertRecoverRequest recoverRequest = OBJECT_MAPPER.readValue(
                """
                {"alertId":9201}
                """, OperationsHealthAlertRecoverRequest.class);
        assertEquals(9201L, recoverRequest.getAlertId());
        assertJsonFields(recoverRequest, "alertId");
    }

    @Test
    void alertResponseJsonFieldsShouldRemainStable() {
        OperationsHealthAlertPageResponse pageResponse = OperationsHealthAlertPageResponse.builder()
                .alertId(9201L)
                .component("database")
                .alertType("HEALTH_DOWN")
                .alertLevel("CRITICAL")
                .alertStatus("ACTIVE")
                .sourceRefType("HEALTH")
                .sourceRefId(9001L)
                .latestCheckId(9101L)
                .message("database down")
                .suggestion("check database")
                .recoveryAction("OPEN_HEALTH_DETAIL")
                .recoveryTarget("{\"route\":\"/operations/dashboard\"}")
                .firstTriggeredAt(new Date(1_719_630_400_000L))
                .lastTriggeredAt(new Date(1_719_630_500_000L))
                .ackedAt(new Date(1_719_630_600_000L))
                .ackedByUserId(1001L)
                .recoveredAt(new Date(1_719_630_700_000L))
                .failureReason("probe timeout")
                .build();
        assertJsonFields(
                pageResponse,
                "alertId",
                "component",
                "alertType",
                "alertLevel",
                "alertStatus",
                "sourceRefType",
                "sourceRefId",
                "latestCheckId",
                "message",
                "suggestion",
                "recoveryAction",
                "recoveryTarget",
                "firstTriggeredAt",
                "lastTriggeredAt",
                "ackedAt",
                "ackedByUserId",
                "recoveredAt",
                "failureReason");

        OperationsHealthAlertSummaryResponse summaryResponse = OperationsHealthAlertSummaryResponse.builder()
                .alertId(9201L)
                .component("database")
                .alertType("HEALTH_DOWN")
                .alertLevel("CRITICAL")
                .alertStatus("ACTIVE")
                .sourceRefType("HEALTH")
                .sourceRefId(9001L)
                .message("database down")
                .suggestion("check database")
                .recoveryAction("OPEN_HEALTH_DETAIL")
                .recoveryTarget("{\"route\":\"/operations/dashboard\"}")
                .lastTriggeredAt(new Date(1_719_630_500_000L))
                .failureReason("probe timeout")
                .build();
        assertJsonFields(
                summaryResponse,
                "alertId",
                "component",
                "alertType",
                "alertLevel",
                "alertStatus",
                "sourceRefType",
                "sourceRefId",
                "message",
                "suggestion",
                "recoveryAction",
                "recoveryTarget",
                "lastTriggeredAt",
                "failureReason");
    }

    private void assertJsonFields(Object value, String... fieldNames) {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
