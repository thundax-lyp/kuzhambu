package com.thundax.kuzhambu.ai.infra.prompt.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVersionDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper.PromptMapper;
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
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_service_config`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_model`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_capability_mapping`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_action_status`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_template_scope`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_prompt_version_current`"));
    }

    @Test
    void seedSqlShouldContainClassicsPromptAndBaseConfigRecords() throws IOException {
        String dataSql = readRequiredSql("db/data/ai.sql");
        String normalized = dataSql.replaceAll("\\s+", " ");

        assertTrue(normalized.contains("INSERT INTO `ai_service_config`"));
        assertTrue(normalized.contains("( 900001, 'PRIMARY', 'OPENAI_COMPATIBLE', 'https://ai.wdit.com.cn/v1'"));
        assertTrue(normalized.contains(
                "900002, 'TEXT2IMAGE', 'OPENAI_COMPATIBLE', 'https://ark.cn-beijing.volces.com/api/v3'"));
        assertTrue(normalized.contains(
                "930106, 'classics', 'translate', 'Classics Translate', 'Classics translate template', 'ACTIVE', 1"));
        assertTrue(normalized.contains(
                "930101, 'classics', 'summary', 'Classics Summary', 'Classics summary template', 'ACTIVE', 1"));
        assertTrue(normalized.contains("900101, 900001, 'CTYUN-CX-Qwen3.5-397B-A17B'"));
        assertTrue(normalized.contains("900102, 900001, 'CTYUN-bot-DeepSeek-V3.2-pro'"));
        assertTrue(normalized.contains("900201, 900002, 'doubao-seedream-5-0-pro-260628'"));
        assertTrue(normalized.contains("(910105, 'classics', 'translate', 900102"));
        assertTrue(normalized.contains("(910106, 'classics', 'image_gen', 900201"));
        assertTrue(normalized.contains("(920106, 'classics', 'image_gen', 1, NULL"));
        assertTrue(normalized.contains("(920107, 'classics', 'translate', 1, NULL"));

        assertTrue(normalized.contains("( 940101, 930101, 1,"));
        assertTrue(normalized.contains("( 940106, 930106, 1,"));
        assertTrue(normalized.contains("\"contentType\",\"required\":true"));
        assertTrue(normalized.contains("\"existingSummary\",\"required\":false"));
        assertTrue(normalized.contains("\"contextPath\",\"required\":false"));
        assertTrue(normalized.contains("\"sourceText\",\"required\":true"));
        assertTrue(normalized.contains("{\"type\":\"text\"}"));
    }

    @Test
    void repositoryShouldMapPromptTemplateWritesAndReads() {
        PromptMapper mapper = mock(PromptMapper.class);
        PromptRepositoryImpl repository = new PromptRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-03T00:00:00Z");
        PromptTemplate template = new PromptTemplate(
                null,
                4001L,
                "classics",
                "translate",
                "Classics translate",
                "translate prompt",
                "ACTIVE",
                1,
                registeredAt);

        Long templateId = repository.saveTemplate(template);

        ArgumentCaptor<PromptTemplateDO> templateCaptor = ArgumentCaptor.forClass(PromptTemplateDO.class);
        verify(mapper).insert(templateCaptor.capture());
        PromptTemplateDO savedTemplate = templateCaptor.getValue();
        assertEquals(4001L, templateId);
        assertEquals("classics", savedTemplate.getScope());
        assertEquals("translate", savedTemplate.getCapability());

        when(mapper.selectTemplateByScope("classics", "translate")).thenReturn(savedTemplate);
        PromptTemplate loadedTemplate = repository.getTemplate("classics", "translate");

        assertEquals("Classics translate", loadedTemplate.getName());
        assertEquals(1, loadedTemplate.getCurrentVersionNo());
    }

    @Test
    void repositoryShouldMapPromptVersionAndVariableWrites() {
        PromptMapper mapper = mock(PromptMapper.class);
        PromptRepositoryImpl repository = new PromptRepositoryImpl(mapper);
        Instant registeredAt = Instant.parse("2026-01-04T00:00:00Z");
        PromptVersion version = new PromptVersion(
                null,
                5001L,
                4001L,
                1,
                "[{\"role\":\"user\",\"content\":\"{{text}}\"}]",
                "[{\"name\":\"text\"}]",
                "{\"type\":\"object\"}",
                "4001:current",
                "initial",
                registeredAt);
        List<PromptVariable> variables = List.of(new PromptVariable(null, 6001L, 4001L, "text", true, "input text", 1));

        when(mapper.markCurrentVersion(4001L, 1)).thenReturn(1);
        repository.saveVersion(version);
        int affectedRows = repository.markCurrentVersion(4001L, 1);
        int variableRows = repository.replaceVariables(4001L, variables);

        ArgumentCaptor<PromptVersionDO> versionCaptor = ArgumentCaptor.forClass(PromptVersionDO.class);
        ArgumentCaptor<PromptVariableDO> variableCaptor = ArgumentCaptor.forClass(PromptVariableDO.class);
        verify(mapper).insertVersion(versionCaptor.capture());
        verify(mapper).clearCurrentVersion(4001L);
        verify(mapper).updateTemplateCurrentVersion(4001L, 1);
        verify(mapper).insertVariable(variableCaptor.capture());

        assertEquals(1, affectedRows);
        assertEquals(0, variableRows);
        assertEquals(5001L, versionCaptor.getValue().getPromptVersionId());
        assertEquals("4001:current", versionCaptor.getValue().getCurrentKey());
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
