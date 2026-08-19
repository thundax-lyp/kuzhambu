package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVariableIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.PromptMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PromptRepositoryIT {

    @Test
    void schemaSqlShouldDeclarePromptPersistenceObjects() throws IOException {
        String schemaSql = readRequiredSql("db/schema/ai.sql");

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_prompt_template`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_prompt_version`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_prompt_variable`"));
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_service_config`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_business_config`"));
        assertFalse(schemaSql.contains("UNIQUE KEY `uk_ai_business_config_capability`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_business_config_priority` (`priority`)"));
        assertTrue(schemaSql.contains("KEY `idx_ai_business_config_capability` (`capability`, `enabled`)"));
        assertTrue(schemaSql.contains("KEY `idx_ai_business_config_enabled` (`enabled`)"));
        assertFalse(schemaSql.contains("KEY `idx_ai_business_config_capability` (`capability`, `enabled`, `priority`"));
        assertFalse(schemaSql.contains("KEY `idx_ai_business_config_enabled` (`enabled`, `priority`)"));
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_capability_mapping`"));
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_action_status`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_template_capability`"));
        assertTrue(schemaSql.contains("KEY `idx_ai_prompt_template_enabled`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_version_no`"));
    }

    @Test
    void repositoryShouldMapPromptTemplateWritesAndReads() {
        PromptMapper mapper = mock(PromptMapper.class);
        PromptRepositoryImpl repository = new PromptRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-03T00:00:00Z");
        PromptTemplate template = new PromptTemplate(
                PromptTemplateIdCodec.toDomain(4001L),
                AiBusinessCapability.CLASSICS_TRANSLATE,
                "Classics translate",
                "translate prompt",
                true,
                1,
                registeredAt);

        PromptTemplateId templateId = repository.insertTemplate(template);

        ArgumentCaptor<PromptTemplateDO> templateCaptor = ArgumentCaptor.forClass(PromptTemplateDO.class);
        verify(mapper).insert(templateCaptor.capture());
        PromptTemplateDO savedTemplate = templateCaptor.getValue();
        assertEquals(4001L, templateId.value());
        assertEquals("CLASSICS_TRANSLATE", savedTemplate.getCapability());
        assertEquals(true, savedTemplate.getEnabled());

        when(mapper.selectTemplateByCapability("CLASSICS_TRANSLATE")).thenReturn(savedTemplate);
        PromptTemplate loadedTemplate = repository.getByCapability(AiBusinessCapability.CLASSICS_TRANSLATE);

        assertEquals("Classics translate", loadedTemplate.getName());
        assertEquals(1, loadedTemplate.getCurrentVersionNo());
    }

    @Test
    void repositoryShouldMapPromptVersionAndVariableWrites() {
        PromptMapper mapper = mock(PromptMapper.class);
        PromptRepositoryImpl repository = new PromptRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-04T00:00:00Z");
        PromptVersion version = new PromptVersion(
                PromptVersionIdCodec.toDomain(5001L),
                PromptTemplateIdCodec.toDomain(4001L),
                1,
                "[{\"role\":\"user\",\"content\":\"{{text}}\"}]",
                "[{\"name\":\"text\"}]",
                "{\"type\":\"object\"}",
                "initial",
                registeredAt);
        List<PromptVariable> variables = List.of(new PromptVariable(
                PromptVariableIdCodec.toDomain(6001L),
                PromptTemplateIdCodec.toDomain(4001L),
                "text",
                true,
                "input text",
                1));

        when(mapper.markCurrentVersion(4001L, 1)).thenReturn(1);
        repository.insertVersion(version);
        int affectedRows = repository.updateCurrentVersion(PromptTemplateIdCodec.toDomain(4001L), 1);
        int variableRows = repository.updateTemplateVariables(PromptTemplateIdCodec.toDomain(4001L), variables);

        ArgumentCaptor<PromptVersionDO> versionCaptor = ArgumentCaptor.forClass(PromptVersionDO.class);
        ArgumentCaptor<PromptVariableDO> variableCaptor = ArgumentCaptor.forClass(PromptVariableDO.class);
        verify(mapper).insertVersion(versionCaptor.capture());
        verify(mapper).insertVariable(variableCaptor.capture());

        assertEquals(1, affectedRows);
        assertEquals(0, variableRows);
        assertEquals(5001L, versionCaptor.getValue().getId());
        assertEquals("text", variableCaptor.getValue().getVariableName());
        assertEquals(4001L, variableCaptor.getValue().getTemplateId());
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
