package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import java.time.Instant;

public interface AiCandidateApplicationService {

    AiCandidate requirePendingForApply(
            AiCandidateId candidateId,
            AiContentRef contentRef,
            AiBusinessCapability capability,
            AiTargetObjectId targetObjectId);

    AiCandidate markApplied(AiCandidateId candidateId, String resultFormat, String resultPayload, Instant appliedAt);

    AiCandidate reject(AiCandidateId candidateId, String errorType, String errorMessage);
}
