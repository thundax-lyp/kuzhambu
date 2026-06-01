package com.thundax.kuzhambu.ai.infra.model.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelCheckRecordDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiServiceConfigDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.mapper.AiModelMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiModelRepositoryIT {

    @Test
    void schemaAndSeedSqlShouldDeclareModelPersistenceObjects() throws IOException {
        String schemaSql = readRequiredSql("db/schema/ai.sql");
        String dataSql = readRequiredSql("db/data/ai.sql");

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_service_config`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model_check_record`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_model_service_name`"));
        assertTrue(dataSql.contains("INSERT INTO `ai_capability`"));
        assertTrue(dataSql.contains("ON DUPLICATE KEY UPDATE"));
    }

    @Test
    void repositoryShouldMapModelWritesAndReads() {
        AiModelMapper mapper = mock(AiModelMapper.class);
        AiModelRepositoryImpl repository = new AiModelRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-01T00:00:00Z");
        AiModel model = new AiModel(
                null,
                2001L,
                1001L,
                "gpt-test",
                "GPT Test",
                List.of("chat", "stream"),
                "{\"temperature\":0.2}",
                "test model",
                true,
                registeredAt);

        Long modelId = repository.saveModel(model);

        ArgumentCaptor<AiModelDO> modelCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(mapper).insert(modelCaptor.capture());
        AiModelDO savedModel = modelCaptor.getValue();
        assertEquals(2001L, modelId);
        assertEquals(1001L, savedModel.getServiceId());
        assertEquals("gpt-test", savedModel.getModelName());
        assertEquals("[\"chat\",\"stream\"]", savedModel.getCapabilityTagsJson());
        assertEquals(registeredAt, savedModel.getRegisteredAt());

        when(mapper.selectOne(any())).thenReturn(savedModel);
        AiModel loadedModel = repository.getModelByModelId(2001L);

        assertNotNull(loadedModel);
        assertEquals(List.of("chat", "stream"), loadedModel.getCapabilityTags());
        assertEquals("GPT Test", loadedModel.getDisplayName());
    }

    @Test
    void repositoryShouldMapServiceConfigAndCheckRecordWrites() {
        AiModelMapper mapper = mock(AiModelMapper.class);
        AiModelRepositoryImpl repository = new AiModelRepositoryImpl(mapper);
        Instant configuredAt = Instant.parse("2026-01-02T00:00:00Z");
        AiServiceConfig config = new AiServiceConfig(
                null,
                1001L,
                "PRIMARY",
                "openai",
                "https://ai.example.internal",
                "encrypted-key",
                true,
                "AVAILABLE",
                configuredAt,
                configuredAt);
        AiModelCheckRecord record = new AiModelCheckRecord(
                null, 3001L, 2001L, 1001L, "gpt-test", "SUCCEEDED", 120, null, null, configuredAt);

        repository.saveServiceConfig(config);
        repository.insertCheckRecord(record);

        ArgumentCaptor<AiServiceConfigDO> configCaptor = ArgumentCaptor.forClass(AiServiceConfigDO.class);
        ArgumentCaptor<AiModelCheckRecordDO> recordCaptor = ArgumentCaptor.forClass(AiModelCheckRecordDO.class);
        verify(mapper).insertServiceConfig(configCaptor.capture());
        verify(mapper).insertCheckRecord(recordCaptor.capture());

        assertEquals("PRIMARY", configCaptor.getValue().getServiceRole());
        assertEquals("AVAILABLE", configCaptor.getValue().getStatus());
        assertEquals(3001L, recordCaptor.getValue().getCheckId());
        assertEquals("SUCCEEDED", recordCaptor.getValue().getStatus());
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
