package com.thundax.kuzhambu.ai.domain.invocation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
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
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "summary", "PENDING");
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability("summary");

        AiCandidate actual = service.requirePendingForApply(check);

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyWithObjectIdShouldMatch() {
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "image_analysis", "PENDING", 111L);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability("image_analysis");

        AiCandidate actual = service.requirePendingForApply(check, 111L);

        assertSame(candidate, actual);
    }

    @Test
    void requirePendingForApplyWithObjectIdShouldFailWhenNotMatch() {
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "image_analysis", "PENDING", 111L);
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability("image_analysis");

        DomainException exception =
                assertThrows(DomainException.class, () -> service.requirePendingForApply(check, 112L));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    @Test
    void requirePendingForApplyShouldFailWhenNotPending() {
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "summary", "REJECTED");
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("SANCAI_ENTRY");
        check.setContentId(2L);
        check.setCapability("summary");

        DomainException exception = assertThrows(DomainException.class, () -> service.requirePendingForApply(check));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
    }

    @Test
    void requirePendingForApplyShouldFailWhenTargetMismatch() {
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "summary", "PENDING");
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(1L);
        check.setContentType("WANGQI_DOCUMENT");
        check.setContentId(2L);
        check.setCapability("summary");

        DomainException exception = assertThrows(DomainException.class, () -> service.requirePendingForApply(check));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    @Test
    void markAppliedShouldUpdateCandidateAndReturnAppliedResult() {
        AiCandidate candidate = candidate(1L, "SANCAI_ENTRY", 2L, "summary", "PENDING");
        FakeRepository repository = new FakeRepository(candidate);
        AiCandidateDomainService service = new AiCandidateDomainService(repository);
        Instant appliedAt = Instant.parse("2026-06-22T02:00:00Z");

        AiCandidate actual = service.markApplied(1L, "JSON", "{\"text\":\"ok\"}", appliedAt);

        assertSame(candidate, actual);
        assertEquals("APPLIED", actual.getStatus());
        assertEquals("JSON", actual.getResultFormat());
        assertEquals("{\"text\":\"ok\"}", actual.getResultPayload());
        assertEquals(appliedAt, actual.getAppliedAt());
        assertEquals(1, repository.getUpdateCandidateCount());
        assertSame(candidate, repository.getLastUpdatedCandidate());
    }

    private AiCandidate candidate(Long id, String contentType, Long contentId, String capability, String status) {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(id);
        candidate.setContentType(contentType);
        candidate.setContentId(contentId);
        candidate.setCapability(capability);
        candidate.setStatus(status);
        return candidate;
    }

    private AiCandidate candidate(
            Long id, String contentType, Long contentId, String capability, String status, Long objectId) {
        AiCandidate candidate = candidate(id, contentType, contentId, capability, status);
        candidate.setObjectId(objectId);
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
        public com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord getCallRecord(Long callId) {
            return null;
        }

        @Override
        public Long insertCallRecord(com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord callRecord) {
            return null;
        }

        @Override
        public int updateCallRecord(com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord callRecord) {
            return 0;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord> listCallRecords(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord> pageCallRecords(
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
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord> listCallRecords(
                String scope, String capability, String serviceRole, Instant requestedAtStart, Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return candidateId.equals(candidate.getCandidateId()) ? candidate : null;
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
