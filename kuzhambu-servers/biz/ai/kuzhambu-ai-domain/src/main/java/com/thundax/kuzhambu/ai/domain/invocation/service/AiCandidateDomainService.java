package com.thundax.kuzhambu.ai.domain.invocation.service;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.time.Instant;
import java.util.Objects;

public class AiCandidateDomainService {

    private final AiInvocationRepository repository;

    public AiCandidateDomainService(AiInvocationRepository repository) {
        this.repository = repository;
    }

    public AiCandidate requirePendingForApply(AiCandidateApplyCheck check) {
        AiCandidate candidate = repository.getCandidate(check.getCandidateId());
        if (candidate == null) {
            throw new DomainException(
                    "AI-INVOCATION-404", "ai.candidate.not-found", "AI candidate not found: " + check.getCandidateId());
        }
        if (!candidate.isPending()) {
            throw new DomainException(
                    "AI-INVOCATION-409",
                    "ai.candidate.not-pending",
                    "AI candidate is not pending: " + check.getCandidateId());
        }
        if (!check.getContentType().equals(candidate.getContentType())
                || !check.getContentId().equals(candidate.getContentId())
                || !check.getCapability().equals(candidate.getCapability())) {
            throw new DomainException(
                    "AI-INVOCATION-409", "ai.candidate.target-mismatch", "AI candidate target mismatch");
        }
        return candidate;
    }

    public AiCandidate requirePendingForApply(AiCandidateApplyCheck check, Long objectId) {
        AiCandidate candidate = requirePendingForApply(check);
        if (!Objects.equals(objectId, candidate.getObjectId())) {
            throw new DomainException(
                    "AI-INVOCATION-409", "ai.candidate.target-mismatch", "AI candidate target mismatch");
        }
        return candidate;
    }

    public AiCandidate markApplied(Long candidateId, String resultFormat, String resultPayload, Instant appliedAt) {
        AiCandidate candidate = repository.getCandidate(candidateId);
        if (candidate == null) {
            throw new DomainException(
                    "AI-INVOCATION-404", "ai.candidate.not-found", "AI candidate not found: " + candidateId);
        }
        requirePending(candidate, candidateId);
        candidate.markApplied(appliedAt);
        candidate.setResultFormat(resultFormat);
        candidate.setResultPayload(resultPayload);
        int updated = repository.updateCandidate(candidate);
        if (updated != 1) {
            throw new DomainException(
                    "AI-INVOCATION-409", "ai.candidate.update-failed", "AI candidate update failed: " + candidateId);
        }
        return candidate;
    }

    public AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
        AiCandidate candidate = repository.getCandidate(candidateId);
        if (candidate == null) {
            throw new DomainException(
                    "AI-INVOCATION-404", "ai.candidate.not-found", "AI candidate not found: " + candidateId);
        }
        requirePending(candidate, candidateId);
        candidate.reject(errorType, errorMessage);
        int updated = repository.updateCandidate(candidate);
        if (updated != 1) {
            throw new DomainException(
                    "AI-INVOCATION-409", "ai.candidate.update-failed", "AI candidate update failed: " + candidateId);
        }
        return candidate;
    }

    private void requirePending(AiCandidate candidate, Long candidateId) {
        if (!candidate.isPending()) {
            throw new DomainException(
                    "AI-INVOCATION-409", "ai.candidate.not-pending", "AI candidate is not pending: " + candidateId);
        }
    }
}
