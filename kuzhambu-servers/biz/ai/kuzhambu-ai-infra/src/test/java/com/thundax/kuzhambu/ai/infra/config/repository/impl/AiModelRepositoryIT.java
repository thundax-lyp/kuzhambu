package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.AiModelMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiModelRepositoryIT {

    @Test
    void repositoryShouldMapModelWritesAndReads() {
        AiModelMapper mapper = mock(AiModelMapper.class);
        AiModelRepositoryImpl repository = new AiModelRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-01T00:00:00Z");
        AiModel model = new AiModel(
                AiModelIdCodec.toDomain(2001L),
                AiApiSource.OPENAI,
                "https://api.example",
                "encrypted",
                AiModelName.of("gpt-test"),
                "GPT Test",
                List.of(AiModelCapability.TEXT2TEXT, AiModelCapability.IMAGE2TEXT),
                "{\"temperature\":0.2}",
                "test model",
                true,
                registeredAt);

        AiModelId modelId = repository.insert(model);

        ArgumentCaptor<AiModelDO> modelCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(mapper).insert(modelCaptor.capture());
        AiModelDO savedModel = modelCaptor.getValue();
        assertEquals(2001L, modelId.value());
        assertEquals("OPENAI", savedModel.getApiSource());
        assertEquals("https://api.example", savedModel.getBaseUrl());
        assertEquals("gpt-test", savedModel.getModelName());
        assertEquals("[\"TEXT2TEXT\",\"IMAGE2TEXT\"]", savedModel.getCapabilitiesJson());
        assertEquals(registeredAt, savedModel.getRegisteredAt());

        when(mapper.selectById(any())).thenReturn(savedModel);
        AiModel loadedModel = repository.getById(AiModelIdCodec.toDomain(2001L));

        assertNotNull(loadedModel);
        assertEquals(List.of(AiModelCapability.TEXT2TEXT, AiModelCapability.IMAGE2TEXT), loadedModel.getCapabilities());
        assertEquals("gpt-test", loadedModel.getModelName().value());
        assertEquals("GPT Test", loadedModel.getDisplayName());
    }
}
