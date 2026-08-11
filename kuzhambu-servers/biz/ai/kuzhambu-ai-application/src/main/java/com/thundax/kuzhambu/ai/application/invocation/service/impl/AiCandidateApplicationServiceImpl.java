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
        AiCandidate candidate = getRequired(query == null ? null : query.candidateId());
        candidate.requirePendingForApply(
                query == null ? null : query.contentRef(), query == null ? null : query.capability());
        if (query != null && query.targetObjectId() != null) {
            candidate.requireTargetObject(query.targetObjectId());
        }
        return candidate;
    }

    @Override
    public AiCandidate markApplied(ApplyAiCandidateCommand command) {
        AiCandidate candidate = getRequired(command == null ? null : command.candidateId());
        candidate.markApplied(
                defaultIfNull(command == null ? null : command.resultFormat(), candidate.getResultFormat()),
                defaultIfNull(command == null ? null : command.resultPayload(), candidate.getResultPayload()),
                command == null || command.appliedAt() == null ? Instant.now() : command.appliedAt());
        updateRequired(candidate);
        return candidate;
    }

    @Override
    public AiCandidate reject(RejectAiCandidateCommand command) {
        AiCandidate candidate = getRequired(command == null ? null : command.candidateId());
        candidate.reject(command == null ? null : command.errorType(), command == null ? null : command.errorMessage());
        updateRequired(candidate);
        return candidate;
    }

    private AiCandidate getRequired(AiCandidateId candidateId) {
        AiCandidate candidate = repository.getCandidateById(candidateId);
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
