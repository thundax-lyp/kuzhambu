package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplyAiCandidateCommand {

    private final AiCandidateId candidateId;
    private final String resultFormat;
    private final String resultPayload;
    private final Instant appliedAt;
}
