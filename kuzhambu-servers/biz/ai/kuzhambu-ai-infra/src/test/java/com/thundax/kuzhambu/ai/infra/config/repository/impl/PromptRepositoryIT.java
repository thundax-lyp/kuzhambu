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
    void seedSqlShouldContainClassicsPromptAndBaseConfigRecords() throws IOException {
        String dataSql = readRequiredSql("db/data/ai.sql");
        String normalized = dataSql.replaceAll("\\s+", " ");

        assertFalse(normalized.contains("INSERT INTO `ai_service_config`"));
        assertFalse(normalized.contains("https://ai.wdit.com.cn/v1"));
        assertFalse(normalized.contains("https://ark.cn-beijing.volces.com/api/v3"));
        assertTrue(normalized.contains("`base_url` = COALESCE(NULLIF(VALUES(`base_url`), ''), `base_url`)"));
        assertTrue(normalized.contains(
                "`encrypted_api_key` = COALESCE(VALUES(`encrypted_api_key`), `encrypted_api_key`)"));
        assertTrue(normalized.contains("1, 'classics_summary', '古籍摘要提示词', '古籍内容默认摘要提示词。', 1, 1"));
        assertTrue(normalized.contains("4, 'discovery_query_understanding', '知识发现查询理解提示词', '知识发现默认查询理解提示词。', 1, 1"));
        assertTrue(normalized.contains("8, 'classics_image_generate', '古籍图片生成提示词', '古籍视觉资产默认文生图提示词。', 1, 1"));
        assertTrue(normalized.contains("1, 'OPENAI_COMPATIBLE', '', NULL, 'CTYUN-CX-Qwen3.5-397B-A17B'"));
        assertTrue(normalized.contains("2, 'OPENAI_COMPATIBLE', '', NULL, 'CTYUN-bot-DeepSeek-V3.2-pro'"));
        assertTrue(normalized.contains("3, 'BYTEDANCE', '', NULL, 'doubao-seedream-5-0-pro-260628'"));
        assertTrue(normalized.contains("INSERT INTO `ai_business_config`"));
        assertTrue(normalized.contains("(6, 'classics_translate', 6, 2, NULL, 1, 6"));
        assertTrue(normalized.contains("(7, 'classics_image_describe', 7, 1, NULL, 1, 7"));
        assertTrue(normalized.contains("(8, 'classics_image_generate', 8, 3, NULL, 1, 8"));
        assertSeedContainsAllBusinessCapabilities(normalized);
        assertTrue(normalized.contains("`model_id` = COALESCE(`model_id`, VALUES(`model_id`))"));
        assertFalse(normalized.contains("INSERT INTO `ai_capability_mapping`"));
        assertFalse(normalized.contains("INSERT INTO `ai_action_status`"));

        assertTrue(normalized.contains("(1, 1, 1,"));
        assertTrue(normalized.contains("(6, 6, 1,"));
        assertTrue(normalized.contains("(8, 8, 1,"));
        assertTrue(normalized.contains("\"variableName\":\"contentType\",\"required\":true"));
        assertTrue(normalized.contains("\"variableName\":\"existingSummary\",\"required\":false"));
        assertTrue(normalized.contains("\"variableName\":\"contextPath\",\"required\":false"));
        assertTrue(normalized.contains("\"variableName\":\"sourceText\",\"required\":true"));
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
                true,
                1,
                registeredAt);

        PromptTemplateId templateId = repository.insertTemplate(template);

        ArgumentCaptor<PromptTemplateDO> templateCaptor = ArgumentCaptor.forClass(PromptTemplateDO.class);
        verify(mapper).insert(templateCaptor.capture());
        PromptTemplateDO savedTemplate = templateCaptor.getValue();
        assertEquals(4001L, templateId.value());
        assertEquals("classics_translate", savedTemplate.getCapability());
        assertEquals(true, savedTemplate.getEnabled());

        when(mapper.selectTemplateByCapability("classics_translate")).thenReturn(savedTemplate);
        PromptTemplate loadedTemplate = repository.get(AiBusinessCapability.CLASSICS_TRANSLATE);

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

    private static void assertSeedContainsAllBusinessCapabilities(String normalizedSql) {
        String promptTemplateSection =
                section(normalizedSql, "INSERT INTO `ai_prompt_template`", "INSERT INTO `ai_business_config`");
        String businessConfigSection =
                section(normalizedSql, "INSERT INTO `ai_business_config`", "INSERT INTO `ai_prompt_version`");
        for (AiBusinessCapability capability : AiBusinessCapability.values()) {
            assertTrue(
                    promptTemplateSection.contains("'" + capability.value() + "'"),
                    "missing prompt template seed for " + capability.value());
            assertTrue(
                    businessConfigSection.contains("'" + capability.value() + "'"),
                    "missing business config seed for " + capability.value());
        }
    }

    private static String section(String value, String startMarker, String endMarker) {
        int start = value.indexOf(startMarker);
        int end = value.indexOf(endMarker, start + startMarker.length());
        if (start < 0 || end < 0) {
            return "";
        }
        return value.substring(start, end);
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
