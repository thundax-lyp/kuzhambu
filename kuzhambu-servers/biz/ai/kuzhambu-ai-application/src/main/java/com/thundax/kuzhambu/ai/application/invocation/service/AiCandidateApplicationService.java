package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;

public interface AiCandidateApplicationService {

    AiCandidate requirePendingForApply(RequireAiCandidateForApplyQuery query);

    AiCandidate markApplied(ApplyAiCandidateCommand command);

    AiCandidate reject(RejectAiCandidateCommand command);

    void cleanup(AiCandidateId candidateId);
}
