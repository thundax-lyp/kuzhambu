package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.batch.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiPromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiInvocationLogDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiInvocationRepositoryIT {

    @Test
    void schemaSqlShouldDeclareInvocationPersistenceObjects() throws IOException {
        String schemaSql = readRequiredSql("db/schema/ai.sql");

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_invocation_log`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_candidate`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_invocation_log_id`"));
        assertTrue(schemaSql.contains("`failure_stage`"));
        assertTrue(schemaSql.contains("`result_format`"));
        assertTrue(schemaSql.contains("`result_payload`"));
        assertTrue(schemaSql.contains("`artifact_reference_json`"));
        assertTrue(schemaSql.contains("`rejected_at`"));
        assertTrue(schemaSql.contains("KEY `idx_ai_invocation_log_trace`"));
        assertTrue(schemaSql.contains("KEY `idx_ai_candidate_target`"));
        assertFalse(readCreateTableBlock(schemaSql, "ai_candidate").contains("`candidate_id`"));
    }

    @Test
    void repositoryShouldMapInvocationLogWritesAndReads() {
        AiInvocationMapper mapper = mock(AiInvocationMapper.class);
        AiInvocationRepositoryImpl repository = new AiInvocationRepositoryImpl(mapper);
        Instant requestedAt = Instant.parse("2026-01-05T00:00:00Z");
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setCallId(AiCallId.of(7001L));
        invocationLog.setBatchId(AiBatchJobId.of(8001L));
        invocationLog.setScope("classics");
        invocationLog.setCapability(AiBusinessCapability.CLASSICS_TRANSLATE);
        invocationLog.setContentRef(AiContentRef.of("ENTRY", 9001L));
        invocationLog.setTargetObjectId(AiTargetObjectId.of(9101L));
        invocationLog.setServiceId(1001L);
        invocationLog.setServiceRole("PRIMARY");
        invocationLog.setModelId(new AiModelId(2001L));
        invocationLog.setModelName(AiModelName.of("gpt-test"));
        invocationLog.setPromptVersionId(new PromptVersionId(5001L));
        invocationLog.setRequestId("req-1");
        invocationLog.setTraceId("trace-1");
        invocationLog.setStatus(AiInvocationStatus.SUCCEEDED);
        invocationLog.setStreamUsed(true);
        invocationLog.setStreamCompleted(true);
        invocationLog.setFallbackUsed(false);
        invocationLog.setFailureStage("WORKER_RESULT");
        invocationLog.setResultFormat("TEXT");
        invocationLog.setResultPayload("译文内容");
        invocationLog.setArtifactReferenceJson("{\"artifact\":\"s3://ai/call/7001.json\"}");
        invocationLog.setUsage(new AiUsageSnapshot(120, 10, 20, new BigDecimal("0.01")));
        invocationLog.setWarningsJson("[]");
        invocationLog.setRequestedAt(requestedAt);
        invocationLog.setCompletedAt(requestedAt);

        Long callId = repository.insertInvocationLog(invocationLog);

        ArgumentCaptor<AiInvocationLogDO> callCaptor = ArgumentCaptor.forClass(AiInvocationLogDO.class);
        verify(mapper).insert(callCaptor.capture());
        AiInvocationLogDO savedCall = callCaptor.getValue();
        assertEquals(7001L, callId);
        assertEquals("classics", savedCall.getScope());
        assertEquals(10, savedCall.getInputTokens());
        assertEquals(new BigDecimal("0.01"), savedCall.getCostAmount());
        assertEquals("WORKER_RESULT", savedCall.getFailureStage());
        assertEquals("TEXT", savedCall.getResultFormat());
        assertEquals("译文内容", savedCall.getResultPayload());
        assertEquals("{\"artifact\":\"s3://ai/call/7001.json\"}", savedCall.getArtifactReferenceJson());

        when(mapper.selectOne(any())).thenReturn(savedCall);
        AiInvocationLog loadedRecord = repository.getInvocationLog(7001L);

        assertEquals("trace-1", loadedRecord.getTraceId());
        assertEquals(20, loadedRecord.getUsage().getOutputTokens());
        assertTrue(loadedRecord.isStreamCompleted());
        assertEquals("WORKER_RESULT", loadedRecord.getFailureStage());
    }

    @Test
    void repositoryShouldMapCandidateWritesAndReads() {
        AiInvocationMapper mapper = mock(AiInvocationMapper.class);
        AiInvocationRepositoryImpl repository = new AiInvocationRepositoryImpl(mapper);
        Instant requestedAt = Instant.parse("2026-01-06T00:00:00Z");
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateId.of(7101L));
        candidate.setCallId(AiCallId.of(7001L));
        candidate.setBatchId(AiBatchJobId.of(8001L));
        candidate.setCapability(AiBusinessCapability.CLASSICS_TRANSLATE);
        candidate.setContentRef(AiContentRef.of("ENTRY", 9001L));
        candidate.setTargetObjectId(AiTargetObjectId.of(9101L));
        candidate.setArtifactReferenceJson("{\"artifact\":\"s3://ai/candidate/7101.json\"}");
        candidate.setResultFormat("json");
        candidate.setResultPayload("{\"title\":\"ok\"}");
        candidate.setFailureStage("SCHEMA_CHECK");
        candidate.setErrorType("WARN");
        candidate.setErrorMessage("临时校验警告");
        candidate.setStatus(AiCandidateStatus.REJECTED);
        candidate.setPromptVersionId(AiPromptVersionId.of(5001L));
        candidate.setModelName(AiModelName.of("gpt-test"));
        candidate.setRequestedAt(requestedAt);
        candidate.setRejectedAt(requestedAt);

        Long candidateId = repository.insertCandidate(candidate);

        ArgumentCaptor<AiCandidateDO> candidateCaptor = ArgumentCaptor.forClass(AiCandidateDO.class);
        verify(mapper).insertCandidate(candidateCaptor.capture());
        AiCandidateDO savedCandidate = candidateCaptor.getValue();
        assertEquals(7101L, candidateId);
        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), savedCandidate.getCapability());
        assertEquals("{\"artifact\":\"s3://ai/candidate/7101.json\"}", savedCandidate.getArtifactReferenceJson());
        assertEquals("json", savedCandidate.getResultFormat());
        assertEquals("REJECTED", savedCandidate.getStatus());
        assertEquals("SCHEMA_CHECK", savedCandidate.getFailureStage());
        assertEquals("WARN", savedCandidate.getErrorType());
        assertEquals(requestedAt, savedCandidate.getRejectedAt());

        when(mapper.selectCandidate(7101L)).thenReturn(savedCandidate);
        when(mapper.selectCandidates("ENTRY", 9001L, null, AiBusinessCapability.CLASSICS_TRANSLATE.value(), "REJECTED"))
                .thenReturn(List.of(savedCandidate));
        AiCandidate loadedCandidate = repository.getCandidate(7101L);
        List<AiCandidate> loadedCandidates = repository.listCandidates(
                "ENTRY", 9001L, null, AiBusinessCapability.CLASSICS_TRANSLATE.value(), "REJECTED");

        assertEquals("{\"title\":\"ok\"}", loadedCandidate.getResultPayload());
        assertEquals("SCHEMA_CHECK", loadedCandidate.getFailureStage());
        assertEquals(AiCandidateStatus.REJECTED, loadedCandidate.getStatus());
        assertEquals(1, loadedCandidates.size());
        assertEquals(7101L, loadedCandidates.get(0).getId().value());
    }

    @Test
    void repositoryShouldPageInvocationLogsWithFilters() {
        AiInvocationMapper mapper = mock(AiInvocationMapper.class);
        AiInvocationRepositoryImpl repository = new AiInvocationRepositoryImpl(mapper);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-02T00:00:00Z");
        AiInvocationLogDO dataObject = new AiInvocationLogDO();
        dataObject.setCallId(7002L);
        dataObject.setScope("classics");
        dataObject.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());
        dataObject.setContentType("SANCAI_ENTRY");
        dataObject.setContentId(9002L);
        dataObject.setStatus("SUCCEEDED");
        dataObject.setServiceRole("PRIMARY");
        dataObject.setModelName("gpt-summary");
        dataObject.setFallbackUsed(false);
        dataObject.setRequestedAt(start);

        when(mapper.countInvocationLogs(
                        "classics", "summary", "SANCAI_ENTRY", 9002L, "SUCCEEDED", "PRIMARY", "gpt", false, start, end))
                .thenReturn(1L);
        when(mapper.selectInvocationLogsPage(
                        "classics",
                        "summary",
                        "SANCAI_ENTRY",
                        9002L,
                        "SUCCEEDED",
                        "PRIMARY",
                        "gpt",
                        false,
                        start,
                        end,
                        20,
                        20))
                .thenReturn(List.of(dataObject));

        PageResult<AiInvocationLog> page = repository.pageInvocationLogs(
                "classics", "summary", "SANCAI_ENTRY", 9002L, "SUCCEEDED", "PRIMARY", "gpt", false, start, end, 2, 20);

        assertEquals(2, page.getPageNo());
        assertEquals(20, page.getPageSize());
        assertEquals(1L, page.getTotalCount());
        assertEquals(1, page.getRecords().size());
        assertEquals(7002L, page.getRecords().get(0).getCallId().value());
    }

    @Test
    void repositoryShouldListInvocationLogsForSummaryWithFilters() {
        AiInvocationMapper mapper = mock(AiInvocationMapper.class);
        AiInvocationRepositoryImpl repository = new AiInvocationRepositoryImpl(mapper);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-02T00:00:00Z");
        AiInvocationLogDO dataObject = new AiInvocationLogDO();
        dataObject.setCallId(7003L);
        dataObject.setCapability(AiBusinessCapability.CLASSICS_TAG_EXTRACT.value());

        when(mapper.selectInvocationLogsForSummary("classics", "tags", "BACKUP", start, end))
                .thenReturn(List.of(dataObject));

        List<AiInvocationLog> records = repository.listInvocationLogs("classics", "tags", "BACKUP", start, end);

        assertEquals(1, records.size());
        assertEquals(7003L, records.get(0).getCallId().value());
        assertEquals(AiBusinessCapability.CLASSICS_TAG_EXTRACT, records.get(0).getCapability());
    }

    private static String readRequiredSql(String path) throws IOException {
        for (Path candidate : List.of(Path.of(path), Path.of("../" + path), Path.of("../../../../" + path))) {
            if (Files.exists(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("Required SQL file not found: " + path);
    }

    private static String readCreateTableBlock(String schemaSql, String tableName) {
        String tableStart = "CREATE TABLE IF NOT EXISTS `" + tableName + "`";
        int start = schemaSql.indexOf(tableStart);
        if (start < 0) {
            return "";
        }
        int end = schemaSql.indexOf(";\n", start);
        return end < 0 ? schemaSql.substring(start) : schemaSql.substring(start, end);
    }
}
