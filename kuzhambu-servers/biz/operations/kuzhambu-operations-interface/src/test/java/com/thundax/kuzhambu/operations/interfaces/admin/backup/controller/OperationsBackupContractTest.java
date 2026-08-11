package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupPageResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OperationsBackupContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void requestJsonFieldsShouldRemainStable() throws Exception {
        OperationsBackupExecuteRequest executeRequest =
                OBJECT_MAPPER.readValue("{}", OperationsBackupExecuteRequest.class);
        assertJsonFields(executeRequest);

        OperationsBackupPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "backupType":"MANUAL",
                  "backupStatus":"SUCCEEDED",
                  "requesterUserId":1001,
                  "pageNo":1,
                  "pageSize":20
                }
                """,
                OperationsBackupPageRequest.class);
        assertEquals("MANUAL", pageRequest.getBackupType());
        assertJsonFields(pageRequest, "backupType", "backupStatus", "requesterUserId", "pageNo", "pageSize");

        OperationsBackupDetailRequest detailRequest = OBJECT_MAPPER.readValue(
                """
                {"backupId":9001}
                """, OperationsBackupDetailRequest.class);
        assertEquals(9001L, detailRequest.getBackupId());
        assertJsonFields(detailRequest, "backupId");
    }

    @Test
    void responseJsonFieldsShouldRemainStable() {
        OperationsBackupExecuteResponse executeResponse = OperationsBackupExecuteResponse.builder()
                .backupId(9001L)
                .backupType("MANUAL")
                .backupStatus("SUCCEEDED")
                .fileName("backup_20260629-120000.sql")
                .fileSizeBytes(4096L)
                .checksum("sha256-backup")
                .failureReason("failed")
                .startedAt(Instant.ofEpochMilli(1_719_630_400_000L))
                .completedAt(Instant.ofEpochMilli(1_719_630_500_000L))
                .expiresAt(Instant.ofEpochMilli(1_722_222_400_000L))
                .build();
        assertJsonFields(
                executeResponse,
                "backupId",
                "backupType",
                "backupStatus",
                "fileName",
                "fileSizeBytes",
                "checksum",
                "failureReason",
                "startedAt",
                "completedAt",
                "expiresAt");

        OperationsBackupPageResponse pageResponse = OperationsBackupPageResponse.builder()
                .backupId(9001L)
                .backupType("MANUAL")
                .backupStatus("SUCCEEDED")
                .fileName("backup_20260629-120000.sql")
                .fileSizeBytes(4096L)
                .checksum("sha256-backup")
                .failureReason("failed")
                .requesterUserId(1001L)
                .startedAt(Instant.ofEpochMilli(1_719_630_400_000L))
                .completedAt(Instant.ofEpochMilli(1_719_630_500_000L))
                .expiresAt(Instant.ofEpochMilli(1_722_222_400_000L))
                .build();
        assertJsonFields(
                pageResponse,
                "backupId",
                "backupType",
                "backupStatus",
                "fileName",
                "fileSizeBytes",
                "checksum",
                "failureReason",
                "requesterUserId",
                "startedAt",
                "completedAt",
                "expiresAt");

        OperationsBackupDetailResponse detailResponse = OperationsBackupDetailResponse.builder()
                .backupId(9001L)
                .backupType("MANUAL")
                .backupStatus("SUCCEEDED")
                .storageObjectId(3001L)
                .fileName("backup_20260629-120000.sql")
                .fileSizeBytes(4096L)
                .checksum("sha256-backup")
                .failureReason("failed")
                .requesterUserId(1001L)
                .startedAt(Instant.ofEpochMilli(1_719_630_400_000L))
                .completedAt(Instant.ofEpochMilli(1_719_630_500_000L))
                .expiresAt(Instant.ofEpochMilli(1_722_222_400_000L))
                .build();
        assertJsonFields(
                detailResponse,
                "backupId",
                "backupType",
                "backupStatus",
                "storageObjectId",
                "fileName",
                "fileSizeBytes",
                "checksum",
                "failureReason",
                "requesterUserId",
                "startedAt",
                "completedAt",
                "expiresAt");
    }

    private void assertJsonFields(Object value, String... fieldNames) {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
