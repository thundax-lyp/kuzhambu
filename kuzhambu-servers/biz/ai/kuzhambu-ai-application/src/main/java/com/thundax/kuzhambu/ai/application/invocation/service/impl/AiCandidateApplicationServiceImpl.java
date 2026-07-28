package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
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
    public AiCandidate requirePendingForApply(
            Long candidateId, String contentType, Long contentId, String capability, Long objectId) {
        AiCandidate candidate = getRequired(AiCandidateIdCodec.toDomain(candidateId));
        AiBusinessCapability expectedCapability = AiBusinessCapability.fromAlias(capability);
        AiContentRef expectedContentRef = AiContentRef.ofNullable(contentType, contentId);
        candidate.requirePendingForApply(expectedContentRef, expectedCapability);
        if (objectId != null) {
            AiTargetObjectId expectedTargetObjectId = AiTargetObjectIdCodec.toDomain(objectId);
            candidate.requireTargetObject(expectedTargetObjectId);
        }
        return candidate;
    }

    @Override
    public AiCandidate markApplied(Long candidateId, String resultFormat, String resultPayload, Instant appliedAt) {
        AiCandidate candidate = getRequired(AiCandidateIdCodec.toDomain(candidateId));
        candidate.markApplied(
                defaultIfNull(resultFormat, candidate.getResultFormat()),
                defaultIfNull(resultPayload, candidate.getResultPayload()),
                appliedAt == null ? Instant.now() : appliedAt);
        updateRequired(candidate);
        return candidate;
    }

    @Override
    public AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
        AiCandidate candidate = getRequired(AiCandidateIdCodec.toDomain(candidateId));
        candidate.reject(errorType, errorMessage);
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
