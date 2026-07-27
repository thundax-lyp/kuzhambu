package com.thundax.kuzhambu.ai.domain.invocation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
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

class AiCandidateDomainServiceTest {

    @Test
    void requirePendingForApplyShouldReturnPendingCandidateWhenTargetMatches() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());

        AiCandidate actual = service.requirePendingForApply(check);

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyWithObjectIdShouldMatch() {
        AiCandidate candidate = candidate(
                1L,
                "SANCAI_ENTRY",
                2L,
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value(),
                AiCandidateStatus.PENDING,
                111L);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value());

        AiCandidate actual = service.requirePendingForApply(check, 111L);

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyWithObjectIdShouldFailWhenNotMatch() {
        AiCandidate candidate = candidate(
                1L,
                "SANCAI_ENTRY",
                2L,
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value(),
                AiCandidateStatus.PENDING,
                111L);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value());

        DomainException exception =
                assertThrows(DomainException.class, () -> service.requirePendingForApply(check, 112L));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    @Test
    void requirePendingForApplyShouldFailWhenNotPending() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.REJECTED);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());

        DomainException exception = assertThrows(DomainException.class, () -> service.requirePendingForApply(check));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
    }

    @Test
    void requirePendingForApplyShouldFailWhenTargetMismatch() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("WANGQI_DOCUMENT");
        check.setContentId(2L);
        check.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());

        DomainException exception = assertThrows(DomainException.class, () -> service.requirePendingForApply(check));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    @Test
    void markAppliedShouldUpdateCandidateAndReturnAppliedResult() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.PENDING);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        Instant appliedAt = Instant.parse("2026-06-22T02:00:00Z");

        AiCandidate actual = service.markApplied(1L, "JSON", "{\"text\":\"ok\"}", appliedAt);

        assertSame(candidate, actual);
        assertEquals(AiCandidateStatus.APPLIED, actual.getStatus());
        assertEquals("JSON", actual.getResultFormat());
        assertEquals("{\"text\":\"ok\"}", actual.getResultPayload());
        assertEquals(appliedAt, actual.getAppliedAt());
        assertEquals(1, repository.getUpdateCandidateCount());
        assertSame(candidate, repository.getLastUpdatedCandidate());
    }

    @Test
    void markAppliedShouldFailWhenCandidateIsNotPending() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.REJECTED);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> service.markApplied(1L, "JSON", "{\"text\":\"ok\"}", Instant.parse("2026-06-22T02:00:00Z")));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
        assertEquals(0, repository.getUpdateCandidateCount());
    }

    @Test
    void rejectShouldFailWhenCandidateIsNotPending() {
        AiCandidate candidate = candidate(
                1L, "SANCAI_ENTRY", 2L, AiBusinessCapability.CLASSICS_SUMMARY.value(), AiCandidateStatus.APPLIED);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);

        DomainException exception =
                assertThrows(DomainException.class, () -> service.reject(1L, "USER_REJECTED", "not useful"));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
        assertEquals(0, repository.getUpdateCandidateCount());
    }

    private AiCandidate candidate(
            Long id, String contentType, Long contentId, String capability, AiCandidateStatus status) {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateId.of(id));
        candidate.setContentRef(AiContentRef.ofNullable(contentType, contentId));
        candidate.setCapability(AiBusinessCapability.from(capability));
        candidate.setStatus(status);
        return candidate;
    }

    private AiCandidate candidate(
            Long id, String contentType, Long contentId, String capability, AiCandidateStatus status, Long objectId) {
        AiCandidate candidate = candidate(id, contentType, contentId, capability, status);
        candidate.setTargetObjectId(AiTargetObjectId.ofNullable(objectId));
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
        public com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog getInvocationLog(Long callId) {
            return null;
        }

        @Override
        public Long insertInvocationLog(
                com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog invocationLog) {
            return null;
        }

        @Override
        public int updateInvocationLog(
                com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog invocationLog) {
            return 0;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog> listInvocationLogs(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog> pageInvocationLogs(
                String scope,
                String capability,
                String contentType,
                Long contentId,
                String status,
                String serviceRole,
                String modelName,
                Boolean fallbackUsed,
                Instant requestedAtStart,
                Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, Collections.emptyList());
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog> listInvocationLogs(
                String scope, String capability, String serviceRole, Instant requestedAtStart, Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return candidateId.equals(candidate.getId().value()) ? candidate : null;
        }

        @Override
        public Long insertCandidate(AiCandidate candidate) {
            return null;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            lastUpdatedCandidate = candidate;
            updateCandidateCount++;
            return 1;
        }

        @Override
        public List<AiCandidate> listCandidates(
                String contentType, Long contentId, Long objectId, String capability, String status) {
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
