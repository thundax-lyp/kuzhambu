package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import com.thundax.kuzhambu.ai.domain.batch.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiPromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidate {

    private AiCandidateId id;
    private AiCallId callId;
    private AiBatchJobId batchId;
    private AiBusinessCapability capability;
    private AiContentRef contentRef;
    private AiTargetObjectId targetObjectId;
    private String artifactReferenceJson;
    private String resultFormat;
    private String resultPayload;
    private AiCandidateStatus status = AiCandidateStatus.PENDING;
    private AiPromptVersionId promptVersionId;
    private AiModelName modelName;
    private String failureStage;
    private String errorType;
    private String errorMessage;
    private Instant requestedAt;
    private Instant appliedAt;
    private Instant rejectedAt;

    public boolean isPending() {
        return AiCandidateStatus.PENDING == status;
    }

    public void reject(String failureType, String failureMessage, String stage, Instant rejectedTime) {
        this.status = AiCandidateStatus.REJECTED;
        this.errorType = failureType;
        this.errorMessage = failureMessage;
        this.failureStage = stage;
        this.rejectedAt = rejectedTime;
    }

    public void reject(String failureType, String failureMessage) {
        reject(failureType, failureMessage, null, Instant.now());
    }

    public void markApplied(Instant appliedTime) {
        this.status = AiCandidateStatus.APPLIED;
        this.appliedAt = appliedTime;
    }
}
