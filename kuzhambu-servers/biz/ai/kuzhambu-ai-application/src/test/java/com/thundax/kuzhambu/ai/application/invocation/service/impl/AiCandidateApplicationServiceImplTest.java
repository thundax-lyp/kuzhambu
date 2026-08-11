package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AiCandidateApplicationServiceImplTest {

    @Test
    void requirePendingForApplyShouldReturnPendingCandidateWhenTargetMatches() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        AiCandidateApplicationServiceImpl service =
                new AiCandidateApplicationServiceImpl(new FakeRepository(candidate));

        AiCandidate actual = service.requirePendingForApply(new RequireAiCandidateForApplyQuery(
                new AiCandidateId(1L),
                AiContentRef.of("SANCAI_ENTRY", 2L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                null));

        assertSame(candidate, actual);
    }

    @ParameterizedTest
    @CsvSource({
        "CLASSICS_SUMMARY, CLASSICS_SUMMARY",
        "CLASSICS_IMAGE_DESCRIBE, CLASSICS_IMAGE_DESCRIBE",
        "KNOWLEDGE_RELATION_EXTRACT, KNOWLEDGE_RELATION_EXTRACT",
        "KNOWLEDGE_GRAPH_EXTRACT, KNOWLEDGE_GRAPH_EXTRACT",
        "KNOWLEDGE_LINEAGE_EXTRACT, KNOWLEDGE_LINEAGE_EXTRACT"
    })
    void requirePendingForApplyShouldUseCapabilityCode(String capabilityCode, AiBusinessCapability expectedCapability) {
        AiCandidate candidate =
                candidate(1L, "SANCAI_ENTRY", 2L, expectedCapability.value(), AiCandidateStatus.PENDING);
        AiCandidateApplicationServiceImpl service =
                new AiCandidateApplicationServiceImpl(new FakeRepository(candidate));

        AiCandidate actual = service.requirePendingForApply(new RequireAiCandidateForApplyQuery(
                new AiCandidateId(1L),
                AiContentRef.of("SANCAI_ENTRY", 2L),
                AiBusinessCapability.from(capabilityCode),
                null));

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyShouldMatchObjectIdWhenPresent() {
        AiCandidate candidate = candidate(
                1L,
                "SANCAI_ENTRY",
                2L,
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value(),
                AiCandidateStatus.PENDING,
                111L);
        AiCandidateApplicationServiceImpl service =
                new AiCandidateApplicationServiceImpl(new FakeRepository(candidate));

        AiCandidate actual = service.requirePendingForApply(new RequireAiCandidateForApplyQuery(
                new AiCandidateId(1L),
                AiContentRef.of("SANCAI_ENTRY", 2L),
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE,
                new AiTargetObjectId(111L)));

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyShouldFailWhenTargetMismatch() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        AiCandidateApplicationServiceImpl service =
                new AiCandidateApplicationServiceImpl(new FakeRepository(candidate));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> service.requirePendingForApply(new RequireAiCandidateForApplyQuery(
                        new AiCandidateId(1L),
                        AiContentRef.of("WANGQI_DOCUMENT", 2L),
                        AiBusinessCapability.CLASSICS_SUMMARY,
                        null)));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    @Test
    void markAppliedShouldUpdateCandidateAndReturnAppliedResult() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateApplicationServiceImpl service = new AiCandidateApplicationServiceImpl(repository);
        Instant appliedAt = Instant.parse("2026-06-22T02:00:00Z");

        AiCandidate actual = service.markApplied(
                new ApplyAiCandidateCommand(new AiCandidateId(1L), "JSON", "{\"text\":\"ok\"}", appliedAt));

        assertSame(candidate, actual);
        assertEquals(AiCandidateStatus.APPLIED, actual.getStatus());
        assertEquals("JSON", actual.getResultFormat());
        assertEquals("{\"text\":\"ok\"}", actual.getResultPayload());
        assertEquals(appliedAt, actual.getAppliedAt());
        assertEquals(1, repository.getUpdateCandidateCount());
        assertSame(candidate, repository.getLastUpdatedCandidate());
    }

    @Test
    void markAppliedShouldFallbackCurrentResultWhenRequestValueIsNull() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        candidate.setResultFormat("TEXT");
        candidate.setResultPayload("existing");
        AiCandidateApplicationServiceImpl service =
                new AiCandidateApplicationServiceImpl(new FakeRepository(candidate));

        AiCandidate actual = service.markApplied(
                new ApplyAiCandidateCommand(new AiCandidateId(1L), null, null, Instant.parse("2026-06-22T02:00:00Z")));

        assertEquals("TEXT", actual.getResultFormat());
        assertEquals("existing", actual.getResultPayload());
    }

    @Test
    void rejectShouldFailWhenCandidateIsNotPending() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.APPLIED);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateApplicationServiceImpl service = new AiCandidateApplicationServiceImpl(repository);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> service.reject(
                        new RejectAiCandidateCommand(new AiCandidateId(1L), "USER_REJECTED", "not useful")));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
        assertEquals(0, repository.getUpdateCandidateCount());
    }

    private AiCandidate candidate(
            Long id, String contentType, Long contentId, String capability, AiCandidateStatus status) {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(id));
        candidate.setContentRef(AiContentRef.ofNullable(contentType, contentId));
        candidate.setCapability(AiBusinessCapability.from(capability));
        candidate.setStatus(status);
        return candidate;
    }

    private AiCandidate candidate(
            Long id, String contentType, Long contentId, String capability, AiCandidateStatus status, Long objectId) {
        AiCandidate candidate = candidate(id, contentType, contentId, capability, status);
        candidate.setTargetObjectId(AiTargetObjectIdCodec.toDomain(objectId));
        return candidate;
    }

    private static class FakeRepository implements AiInvocationRepository {

        private final AiCandidate candidate;
        private AiCandidate lastUpdatedCandidate;
        private int updateCandidateCount;

        FakeRepository(AiCandidate candidate) {
            this.candidate = candidate;
        }

        @Override
        public AiInvocationLog getByCallId(AiCallId callId) {
            return null;
        }

        @Override
        public AiCallId insertInvocationLog(AiInvocationLog invocationLog) {
            return AiCallIdCodec.toDomain(1L);
        }

        @Override
        public int updateInvocationLog(AiInvocationLog invocationLog) {
            return 0;
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<AiInvocationLog> pageByFilter(
                String scope,
                AiBusinessCapability capability,
                AiContentRef contentRef,
                AiInvocationStatus status,
                String serviceRole,
                AiModelName modelName,
                Boolean fallbackUsed,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, Collections.emptyList());
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                String scope,
                AiBusinessCapability capability,
                String serviceRole,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public AiCandidate getByCandidateId(AiCandidateId candidateId) {
            return candidate;
        }

        @Override
        public AiCandidateId insertCandidate(AiCandidate candidate) {
            return AiCandidateIdCodec.toDomain(1L);
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            lastUpdatedCandidate = candidate;
            updateCandidateCount++;
            return 1;
        }

        @Override
        public List<AiCandidate> listCandidates(
                AiContentRef contentRef,
                AiTargetObjectId targetObjectId,
                AiBusinessCapability capability,
                AiCandidateStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<AiCandidate> listCandidatesByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return Collections.emptyList();
        }

        int getUpdateCandidateCount() {
            return updateCandidateCount;
        }

        AiCandidate getLastUpdatedCandidate() {
            return lastUpdatedCandidate;
        }
    }
}
