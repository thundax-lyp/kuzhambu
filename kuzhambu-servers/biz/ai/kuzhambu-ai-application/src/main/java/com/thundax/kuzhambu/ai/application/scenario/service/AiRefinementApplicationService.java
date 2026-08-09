package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import java.util.function.Consumer;

public interface AiRefinementApplicationService {

    default AiRefinementRequestCommand snapshotInvokeConfig(AiRefinementRequestCommand command) {
        return command;
    }

    default void validateSnapshotInvokeConfig(AiRefinementRequestCommand command) {}

    AiCandidateResult translate(AiRefinementRequestCommand command);

    AiCandidateResult summarize(AiRefinementRequestCommand command);

    AiCandidateResult generateTags(AiRefinementRequestCommand command);

    AiCandidateResult generateQa(AiRefinementRequestCommand command);

    AiCandidateResult analyzeImage(AiRefinementRequestCommand command);

    default AiCandidateResult analyzeImage(
            AiRefinementRequestCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        return analyzeImage(command);
    }

    AiCandidateResult fuseVisualContext(AiRefinementRequestCommand command);

    AiCandidateResult generateImage(AiRefinementRequestCommand command);

    default AiCandidateResult generateImage(
            AiRefinementRequestCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        return generateImage(command);
    }

    AiCandidateResult describeVisual(AiRefinementRequestCommand command);

    AiCandidateResult splitEntry(AiRefinementRequestCommand command);
}
