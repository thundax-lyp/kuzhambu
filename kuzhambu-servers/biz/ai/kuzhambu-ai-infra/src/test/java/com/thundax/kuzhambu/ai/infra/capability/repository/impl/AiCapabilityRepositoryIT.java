package com.thundax.kuzhambu.ai.infra.capability.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.mapper.AiCapabilityMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiCapabilityRepositoryIT {

    @Test
    void schemaSqlShouldDeclareActionStatusFields() throws IOException {
        String schemaSql = readRequiredSql("db/schema/ai.sql");

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_action_status`"));
        assertTrue(schemaSql.contains("`action_status_id`"));
        assertTrue(schemaSql.contains("`scope`"));
        assertTrue(schemaSql.contains("`capability`"));
        assertTrue(schemaSql.contains("`available`"));
        assertTrue(schemaSql.contains("`unavailable_reason`"));
        assertTrue(schemaSql.contains("`checked_at`"));
    }

    @Test
    void repositoryShouldListActionStatusesWithFilters() {
        AiCapabilityMapper mapper = mock(AiCapabilityMapper.class);
        AiCapabilityRepositoryImpl repository = new AiCapabilityRepositoryImpl(mapper);
        Instant checkedAt = Instant.parse("2026-01-01T00:00:00Z");
        AiActionStatusDO dataObject = new AiActionStatusDO();
        dataObject.setActionStatusId(7001L);
        dataObject.setScope("classics");
        dataObject.setCapability("summary");
        dataObject.setAvailable(false);
        dataObject.setUnavailableReason("MODEL_DISABLED");
        dataObject.setCheckedAt(checkedAt);

        when(mapper.selectActionStatuses("classics", "summary", false)).thenReturn(List.of(dataObject));

        List<AiActionStatus> statuses = repository.listActionStatuses("classics", "summary", false);

        assertEquals(1, statuses.size());
        assertEquals(7001L, statuses.get(0).getActionStatusId());
        assertEquals("classics", statuses.get(0).getScope());
        assertEquals("summary", statuses.get(0).getCapability());
        assertEquals(false, statuses.get(0).isAvailable());
        assertEquals("MODEL_DISABLED", statuses.get(0).getUnavailableReason());
        assertEquals(checkedAt, statuses.get(0).getCheckedAt());
    }

    private static String readRequiredSql(String path) throws IOException {
        for (Path candidate : List.of(Path.of(path), Path.of("../" + path), Path.of("../../../../" + path))) {
            if (Files.exists(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("Required SQL file not found: " + path);
    }
}
