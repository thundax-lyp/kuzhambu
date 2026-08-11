package com.thundax.kuzhambu.operations.interfaces.admin.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskPageResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OperationsTaskContractTest {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final Instant STARTED_AT = Instant.parse("2026-06-18T01:02:03.456Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-06-18T02:03:04.567Z");
    private static final Instant SNAPSHOT_AT = Instant.parse("2026-06-18T02:03:05.678Z");

    @Test
    void detailResponseShouldSerializeInstantFieldsAsUtcTimestamps() {
        OperationsTaskDetailResponse response = OperationsTaskDetailResponse.builder()
                .startedAt(STARTED_AT)
                .completedAt(COMPLETED_AT)
                .snapshotAt(SNAPSHOT_AT)
                .build();

        var json = OBJECT_MAPPER.valueToTree(response);

        assertEquals("2026-06-18T01:02:03.456Z", json.get("startedAt").asText());
        assertEquals("2026-06-18T02:03:04.567Z", json.get("completedAt").asText());
        assertEquals("2026-06-18T02:03:05.678Z", json.get("snapshotAt").asText());
    }

    @Test
    void pageResponseShouldPreserveNullTimeFields() {
        OperationsTaskPageResponse response = OperationsTaskPageResponse.builder()
                .startedAt(STARTED_AT)
                .completedAt(null)
                .snapshotAt(SNAPSHOT_AT)
                .build();

        var json = OBJECT_MAPPER.valueToTree(response);

        assertEquals("2026-06-18T01:02:03.456Z", json.get("startedAt").asText());
        assertFalse(json.has("completedAt"));
        assertEquals("2026-06-18T02:03:05.678Z", json.get("snapshotAt").asText());
    }
}
