package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AiCandidateApplicationServiceImpl implements AiCandidateApplicationService {

    private final AiInvocationRepository repository;

    public AiCandidateApplicationServiceImpl(AiInvocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public AiCandidate requirePendingForApply(RequireAiCandidateForApplyQuery query) {
        AiCandidate candidate = getRequired(query == null ? null : query.getCandidateId());
        candidate.requirePendingForApply(
                query == null ? null : query.getContentRef(), query == null ? null : query.getCapability());
        if (query != null && query.getTargetObjectId() != null) {
            candidate.requireTargetObject(query.getTargetObjectId());
        }
        return candidate;
    }

    @Override
    public AiCandidate markApplied(ApplyAiCandidateCommand command) {
        AiCandidate candidate = getRequired(command == null ? null : command.getCandidateId());
        candidate.markApplied(
                defaultIfNull(command == null ? null : command.getResultFormat(), candidate.getResultFormat()),
                defaultIfNull(command == null ? null : command.getResultPayload(), candidate.getResultPayload()),
                command == null || command.getAppliedAt() == null ? Instant.now() : command.getAppliedAt());
        updateRequired(candidate);
        return candidate;
    }

    @Override
    public AiCandidate reject(RejectAiCandidateCommand command) {
        AiCandidate candidate = getRequired(command == null ? null : command.getCandidateId());
        candidate.reject(
                command == null ? null : command.getErrorType(), command == null ? null : command.getErrorMessage());
        updateRequired(candidate);
        return candidate;
    }

    private AiCandidate getRequired(AiCandidateId candidateId) {
        AiCandidate candidate = repository.getCandidate(candidateId);
        if (candidate == null) {
            throw new DomainException(
                    "AI-INVOCATION-404", "ai.candidate.not-found", "AI candidate not found: " + candidateId);
        }
        return candidate;
    }

    private void updateRequired(AiCandidate candidate) {
        int updated = repository.updateCandidate(candidate);
        if (updated != 1) {
            throw new DomainException(
                    "AI-INVOCATION-409",
                    "ai.candidate.update-failed",
                    "AI candidate update failed: " + candidate.getId());
        }
    }

    private String defaultIfNull(String requestValue, String currentValue) {
        return requestValue == null ? currentValue : requestValue;
    }
}
