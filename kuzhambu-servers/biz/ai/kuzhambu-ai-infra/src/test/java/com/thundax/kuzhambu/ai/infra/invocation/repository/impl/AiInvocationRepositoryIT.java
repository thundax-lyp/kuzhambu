package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCallRecordDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiInvocationMapper;
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

        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_call_record`"));
        assertTrue(schemaSql.contains("CREATE TABLE IF NOT EXISTS `ai_candidate`"));
        assertTrue(schemaSql.contains("UNIQUE KEY `uk_ai_call_record_id`"));
        assertTrue(schemaSql.contains("`failure_stage`"));
        assertTrue(schemaSql.contains("`result_format`"));
        assertTrue(schemaSql.contains("`result_payload`"));
        assertTrue(schemaSql.contains("`artifact_reference_json`"));
        assertTrue(schemaSql.contains("`rejected_at`"));
        assertTrue(schemaSql.contains("KEY `idx_ai_call_record_trace`"));
        assertTrue(schemaSql.contains("KEY `idx_ai_candidate_target`"));
    }

    @Test
    void repositoryShouldMapCallRecordWritesAndReads() {
        AiInvocationMapper mapper = mock(AiInvocationMapper.class);
        AiInvocationRepositoryImpl repository = new AiInvocationRepositoryImpl(mapper);
        Instant requestedAt = Instant.parse("2026-01-05T00:00:00Z");
        AiCallRecord record = new AiCallRecord();
        record.setCallId(7001L);
        record.setBatchId(8001L);
        record.setScope("classics");
        record.setCapability("translate");
        record.setContentType("ENTRY");
        record.setContentId(9001L);
        record.setObjectId(9101L);
        record.setServiceId(1001L);
        record.setServiceRole("PRIMARY");
        record.setModelId(2001L);
        record.setModelName("gpt-test");
        record.setPromptVersionId(5001L);
        record.setRequestId("req-1");
        record.setTraceId("trace-1");
        record.setStatus("SUCCEEDED");
        record.setStreamUsed(true);
        record.setStreamCompleted(true);
        record.setFallbackUsed(false);
        record.setFailureStage("WORKER_RESULT");
        record.setResultFormat("TEXT");
        record.setResultPayload("译文内容");
        record.setArtifactReferenceJson("{\"artifact\":\"s3://ai/call/7001.json\"}");
        record.setUsage(new AiUsageSnapshot(120, 10, 20, new BigDecimal("0.01")));
        record.setWarningsJson("[]");
        record.setRequestedAt(requestedAt);
        record.setCompletedAt(requestedAt);

        Long callId = repository.saveCallRecord(record);

        ArgumentCaptor<AiCallRecordDO> callCaptor = ArgumentCaptor.forClass(AiCallRecordDO.class);
        verify(mapper).insert(callCaptor.capture());
        AiCallRecordDO savedCall = callCaptor.getValue();
        assertEquals(7001L, callId);
        assertEquals("classics", savedCall.getScope());
        assertEquals(10, savedCall.getInputTokens());
        assertEquals(new BigDecimal("0.01"), savedCall.getCostAmount());
        assertEquals("WORKER_RESULT", savedCall.getFailureStage());
        assertEquals("TEXT", savedCall.getResultFormat());
        assertEquals("译文内容", savedCall.getResultPayload());
        assertEquals("{\"artifact\":\"s3://ai/call/7001.json\"}", savedCall.getArtifactReferenceJson());

        when(mapper.selectOne(any())).thenReturn(savedCall);
        AiCallRecord loadedRecord = repository.getCallRecord(7001L);

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
        candidate.setCandidateId(7101L);
        candidate.setCallId(7001L);
        candidate.setBatchId(8001L);
        candidate.setCapability("translate");
        candidate.setContentType("ENTRY");
        candidate.setContentId(9001L);
        candidate.setObjectId(9101L);
        candidate.setArtifactReferenceJson("{\"artifact\":\"s3://ai/candidate/7101.json\"}");
        candidate.setResultFormat("json");
        candidate.setResultPayload("{\"title\":\"ok\"}");
        candidate.setFailureStage("SCHEMA_CHECK");
        candidate.setErrorType("WARN");
        candidate.setErrorMessage("临时校验警告");
        candidate.setStatus("REJECTED");
        candidate.setPromptVersionId(5001L);
        candidate.setModelName("gpt-test");
        candidate.setRequestedAt(requestedAt);
        candidate.setRejectedAt(requestedAt);

        Long candidateId = repository.saveCandidate(candidate);

        ArgumentCaptor<AiCandidateDO> candidateCaptor = ArgumentCaptor.forClass(AiCandidateDO.class);
        verify(mapper).insertCandidate(candidateCaptor.capture());
        AiCandidateDO savedCandidate = candidateCaptor.getValue();
        assertEquals(7101L, candidateId);
        assertEquals("{\"artifact\":\"s3://ai/candidate/7101.json\"}", savedCandidate.getArtifactReferenceJson());
        assertEquals("json", savedCandidate.getResultFormat());
        assertEquals("REJECTED", savedCandidate.getStatus());
        assertEquals("SCHEMA_CHECK", savedCandidate.getFailureStage());
        assertEquals("WARN", savedCandidate.getErrorType());
        assertEquals(requestedAt, savedCandidate.getRejectedAt());

        when(mapper.selectCandidate(7101L)).thenReturn(savedCandidate);
        when(mapper.selectCandidates("ENTRY", 9001L, "translate", "REJECTED")).thenReturn(List.of(savedCandidate));
        AiCandidate loadedCandidate = repository.getCandidate(7101L);
        List<AiCandidate> loadedCandidates = repository.listCandidates("ENTRY", 9001L, "translate", "REJECTED");

        assertEquals("{\"title\":\"ok\"}", loadedCandidate.getResultPayload());
        assertEquals("SCHEMA_CHECK", loadedCandidate.getFailureStage());
        assertEquals("REJECTED", loadedCandidate.getStatus());
        assertEquals(1, loadedCandidates.size());
        assertEquals(7101L, loadedCandidates.get(0).getCandidateId());
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
