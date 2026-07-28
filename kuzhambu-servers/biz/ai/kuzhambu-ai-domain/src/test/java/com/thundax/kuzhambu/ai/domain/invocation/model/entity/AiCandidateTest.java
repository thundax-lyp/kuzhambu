package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AiCandidateTest {

    @Test
    void markAppliedShouldFailWhenCandidateIsNotPending() {
        AiCandidate candidate = candidate(AiCandidateStatus.REJECTED);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> candidate.markApplied("JSON", "{\"text\":\"ok\"}", Instant.parse("2026-06-22T02:00:00Z")));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
    }

    @Test
    void rejectShouldFailWhenCandidateIsNotPending() {
        AiCandidate candidate = candidate(AiCandidateStatus.APPLIED);

        DomainException exception =
                assertThrows(DomainException.class, () -> candidate.reject("USER_REJECTED", "not useful"));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.not-pending", exception.getMessageKey());
    }

    @Test
    void requirePendingForApplyShouldFailWhenTargetMismatch() {
        AiCandidate candidate = candidate(AiCandidateStatus.PENDING);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> candidate.requirePendingForApply(
                        AiContentRef.of("WANGQI_DOCUMENT", 2L), AiBusinessCapability.CLASSICS_SUMMARY));

        assertEquals("AI-INVOCATION-409", exception.getCode());
        assertEquals("ai.candidate.target-mismatch", exception.getMessageKey());
    }

    private AiCandidate candidate(AiCandidateStatus status) {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(1L));
        candidate.setStatus(status);
        candidate.setContentRef(AiContentRef.of("SANCAI_ENTRY", 2L));
        candidate.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        return candidate;
    }
}
