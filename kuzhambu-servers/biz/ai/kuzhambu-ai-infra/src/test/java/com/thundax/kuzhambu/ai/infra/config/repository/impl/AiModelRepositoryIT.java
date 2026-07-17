package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.AiModelMapper;
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

        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_service_config`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model`"));
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model_check_record`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_model_source_name`"));
        assertFalse(dataSql.contains("INSERT INTO `ai_capability`"));
        assertTrue(dataSql.contains("ON DUPLICATE KEY UPDATE"));
    }

    @Test
    void repositoryShouldMapModelWritesAndReads() {
        AiModelMapper mapper = mock(AiModelMapper.class);
        AiModelRepositoryImpl repository = new AiModelRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-01T00:00:00Z");
        AiModel model = new AiModel(
                AiModelId.of(2001L),
                AiApiSource.OPENAI,
                "https://api.example",
                "encrypted",
                "gpt-test",
                "GPT Test",
                List.of(AiModelCapability.TEXT_TO_TEXT, AiModelCapability.IMAGE_TO_TEXT),
                "{\"temperature\":0.2}",
                "test model",
                true,
                registeredAt);

        AiModelId modelId = repository.saveModel(model);

        ArgumentCaptor<AiModelDO> modelCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(mapper).insert(modelCaptor.capture());
        AiModelDO savedModel = modelCaptor.getValue();
        assertEquals(2001L, modelId.value());
        assertEquals("OPENAI", savedModel.getApiSource());
        assertEquals("https://api.example", savedModel.getBaseUrl());
        assertEquals("gpt-test", savedModel.getModelName());
        assertEquals("[\"TEXT_TO_TEXT\",\"IMAGE_TO_TEXT\"]", savedModel.getCapabilitiesJson());
        assertEquals(registeredAt, savedModel.getRegisteredAt());

        when(mapper.selectById(any())).thenReturn(savedModel);
        AiModel loadedModel = repository.getModelById(AiModelId.of(2001L));

        assertNotNull(loadedModel);
        assertEquals(
                List.of(AiModelCapability.TEXT_TO_TEXT, AiModelCapability.IMAGE_TO_TEXT),
                loadedModel.getCapabilities());
        assertEquals("GPT Test", loadedModel.getDisplayName());
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
