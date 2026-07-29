package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;

public class RequireAiCandidateForApplyQuery {

    private final AiCandidateId candidateId;
    private final AiContentRef contentRef;
    private final AiBusinessCapability capability;
    private final AiTargetObjectId targetObjectId;

    public RequireAiCandidateForApplyQuery(
            AiCandidateId candidateId,
            AiContentRef contentRef,
            AiBusinessCapability capability,
            AiTargetObjectId targetObjectId) {
        this.candidateId = candidateId;
        this.contentRef = contentRef;
        this.capability = capability;
        this.targetObjectId = targetObjectId;
    }

    public AiCandidateId getCandidateId() {
        return candidateId;
    }

    public AiContentRef getContentRef() {
        return contentRef;
    }

    public AiBusinessCapability getCapability() {
        return capability;
    }

    public AiTargetObjectId getTargetObjectId() {
        return targetObjectId;
    }
}
