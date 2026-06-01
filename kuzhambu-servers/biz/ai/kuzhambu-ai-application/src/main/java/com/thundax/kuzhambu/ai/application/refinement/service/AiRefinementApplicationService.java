package com.thundax.kuzhambu.ai.application.refinement.service;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;

public interface AiRefinementApplicationService {

    AiCandidateResult translate(AiRefinementRequestCommand command);

    AiCandidateResult summarize(AiRefinementRequestCommand command);

    AiCandidateResult generateTags(AiRefinementRequestCommand command);

    AiCandidateResult generateQa(AiRefinementRequestCommand command);

    AiCandidateResult analyzeImage(AiRefinementRequestCommand command);

    AiCandidateResult describeVisual(AiRefinementRequestCommand command);

    AiCandidateResult splitEntry(AiRefinementRequestCommand command);
}
