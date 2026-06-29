package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestorePageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestorePageResponse;
import java.util.Date;
import org.junit.jupiter.api.Test;

class OperationsRestoreContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void requestJsonFieldsShouldRemainStable() throws Exception {
        OperationsRestoreExecuteRequest executeRequest = OBJECT_MAPPER.readValue(
                """
                {"backupId":9001}
                """, OperationsRestoreExecuteRequest.class);
        assertEquals(9001L, executeRequest.getBackupId());
        assertJsonFields(executeRequest, "backupId");

        OperationsRestorePageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "backupId":9001,
                  "restoreStatus":"SUCCEEDED",
                  "requesterUserId":1001,
                  "pageNo":1,
                  "pageSize":20
                }
                """,
                OperationsRestorePageRequest.class);
        assertEquals("SUCCEEDED", pageRequest.getRestoreStatus());
        assertJsonFields(pageRequest, "backupId", "restoreStatus", "requesterUserId", "pageNo", "pageSize");

        OperationsRestoreDetailRequest detailRequest = OBJECT_MAPPER.readValue(
                """
                {"restoreId":9101}
                """, OperationsRestoreDetailRequest.class);
        assertEquals(9101L, detailRequest.getRestoreId());
        assertJsonFields(detailRequest, "restoreId");
    }

    @Test
    void responseJsonFieldsShouldRemainStable() {
        OperationsRestoreExecuteResponse executeResponse = OperationsRestoreExecuteResponse.builder()
                .restoreId(9101L)
                .backupId(9001L)
                .preRestoreBackupId(9201L)
                .restoreStatus("SUCCEEDED")
                .writeBlockEnabled(Boolean.TRUE)
                .failureReason(null)
                .startedAt(new Date(1_719_630_400_000L))
                .completedAt(new Date(1_719_630_500_000L))
                .build();
        assertJsonFields(
                executeResponse,
                "restoreId",
                "backupId",
                "preRestoreBackupId",
                "restoreStatus",
                "writeBlockEnabled",
                "failureReason",
                "startedAt",
                "completedAt");

        OperationsRestorePageResponse pageResponse = OperationsRestorePageResponse.builder()
                .restoreId(9101L)
                .backupId(9001L)
                .preRestoreBackupId(9201L)
                .restoreStatus("SUCCEEDED")
                .writeBlockEnabled(Boolean.TRUE)
                .failureReason(null)
                .requesterUserId(1001L)
                .startedAt(new Date(1_719_630_400_000L))
                .completedAt(new Date(1_719_630_500_000L))
                .build();
        assertJsonFields(
                pageResponse,
                "restoreId",
                "backupId",
                "preRestoreBackupId",
                "restoreStatus",
                "writeBlockEnabled",
                "failureReason",
                "requesterUserId",
                "startedAt",
                "completedAt");

        OperationsRestoreDetailResponse detailResponse = OperationsRestoreDetailResponse.builder()
                .restoreId(9101L)
                .backupId(9001L)
                .preRestoreBackupId(9201L)
                .restoreStatus("SUCCEEDED")
                .writeBlockEnabled(Boolean.TRUE)
                .failureReason(null)
                .requesterUserId(1001L)
                .startedAt(new Date(1_719_630_400_000L))
                .completedAt(new Date(1_719_630_500_000L))
                .build();
        assertJsonFields(
                detailResponse,
                "restoreId",
                "backupId",
                "preRestoreBackupId",
                "restoreStatus",
                "writeBlockEnabled",
                "failureReason",
                "requesterUserId",
                "startedAt",
                "completedAt");
    }

    private void assertJsonFields(Object value, String... fieldNames) {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
