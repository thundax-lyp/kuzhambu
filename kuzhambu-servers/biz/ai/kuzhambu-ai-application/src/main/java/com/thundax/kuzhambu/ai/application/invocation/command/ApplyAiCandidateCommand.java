package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import java.time.Instant;

public record ApplyAiCandidateCommand(
        AiCandidateId candidateId, String resultFormat, String resultPayload, Instant appliedAt) {}
