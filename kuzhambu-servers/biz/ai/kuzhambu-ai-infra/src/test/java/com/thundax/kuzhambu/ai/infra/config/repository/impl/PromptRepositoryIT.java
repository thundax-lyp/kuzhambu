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
import com.thundax.kuzhambu.ai.domain.config.model.enums.PromptTemplateStatus;
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
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_capability_mapping`"));
        assertFalse(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_action_status`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_template_capability`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_version_no`"));
    }

    @Test
    void seedSqlShouldContainClassicsPromptAndBaseConfigRecords() throws IOException {
        String dataSql = readRequiredSql("db/data/ai.sql");
        String normalized = dataSql.replaceAll("\\s+", " ");

        assertFalse(normalized.contains("INSERT INTO `ai_service_config`"));
        assertFalse(normalized.contains("https://ai.wdit.com.cn/v1"));
        assertFalse(normalized.contains("https://ark.cn-beijing.volces.com/api/v3"));
        assertTrue(normalized.contains("`base_url` = VALUES(`base_url`)"));
        assertTrue(normalized.contains("`encrypted_api_key` = VALUES(`encrypted_api_key`)"));
        assertTrue(normalized.contains(
                "930101, 'classics_summary', 'Classics Default Summary', '古籍内容默认摘要提示词。', 'ACTIVE', 1"));
        assertTrue(
                normalized.contains(
                        "930104, 'discovery_query_understanding', 'Discovery Default Query Understanding', '知识发现默认查询理解提示词。', 'ACTIVE', 1"));
        assertTrue(
                normalized.contains(
                        "930108, 'classics_image_generate', 'Classics Default Image Generation', '古籍视觉资产默认文生图提示词。', 'ACTIVE', 1"));
        assertTrue(normalized.contains("900101, 'OPENAI', '', NULL, 'CTYUN-CX-Qwen3.5-397B-A17B'"));
        assertTrue(normalized.contains("900102, 'OPENAI', '', NULL, 'CTYUN-bot-DeepSeek-V3.2-pro'"));
        assertTrue(normalized.contains("900201, 'BYTEDANCE', '', NULL, 'doubao-seedream-5-0-pro-260628'"));
        assertTrue(normalized.contains("(910105, 'classics', 'classics_translate', 900102"));
        assertTrue(normalized.contains("(910106, 'classics', 'classics_image_generate', 900201"));
        assertTrue(normalized.contains("(910201, 'discovery', 'discovery_query_understanding', 900102"));
        assertTrue(normalized.contains("(920106, 'classics', 'classics_image_generate', 1, NULL"));
        assertTrue(normalized.contains("(920201, 'discovery', 'discovery_query_understanding', 1, NULL"));

        assertTrue(normalized.contains("(940101, 930101, 1,"));
        assertTrue(normalized.contains("(940106, 930106, 1,"));
        assertTrue(normalized.contains("(940108, 930108, 1,"));
        assertTrue(normalized.contains("\"contentType\",\"required\":true"));
        assertTrue(normalized.contains("\"existingSummary\",\"required\":false"));
        assertTrue(normalized.contains("\"contextPath\",\"required\":false"));
        assertTrue(normalized.contains("\"sourceText\",\"required\":true"));
        assertTrue(normalized.contains("{\"type\":\"text\"}"));
        assertTrue(existsInKnownRoots("db/data-source/ai-prompts/classics/summary/system-template.txt"));
        assertTrue(existsInKnownRoots("db/data-source/ai-prompts/discovery/answer-generation/sample.md"));
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
                PromptTemplateStatus.ACTIVE,
                1,
                registeredAt);

        PromptTemplateId templateId = repository.insertTemplate(template);

        ArgumentCaptor<PromptTemplateDO> templateCaptor = ArgumentCaptor.forClass(PromptTemplateDO.class);
        verify(mapper).insert(templateCaptor.capture());
        PromptTemplateDO savedTemplate = templateCaptor.getValue();
        assertEquals(4001L, templateId.value());
        assertEquals("classics_translate", savedTemplate.getCapability());

        when(mapper.selectTemplateByCapability("classics_translate")).thenReturn(savedTemplate);
        PromptTemplate loadedTemplate = repository.get("classics_translate");

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
        int affectedRows = repository.markCurrentVersion(PromptTemplateIdCodec.toDomain(4001L), 1);
        int variableRows = repository.replaceVariables(PromptTemplateIdCodec.toDomain(4001L), variables);

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

    private static boolean existsInKnownRoots(String path) {
        for (Path candidate : List.of(Path.of(path), Path.of("../" + path), Path.of("../../../../" + path))) {
            if (Files.exists(candidate)) {
                return true;
            }
        }
        return false;
    }
}
