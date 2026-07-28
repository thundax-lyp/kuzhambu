package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import java.time.Instant;

public interface AiCandidateApplicationService {

    AiCandidate requirePendingForApply(
            Long candidateId, String contentType, Long contentId, String capability, Long objectId);

    AiCandidate markApplied(Long candidateId, String resultFormat, String resultPayload, Instant appliedAt);

    AiCandidate reject(Long candidateId, String errorType, String errorMessage);
}
