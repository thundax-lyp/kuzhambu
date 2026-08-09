package com.thundax.kuzhambu.ai.application.invocation.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeContext;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeModelConfig;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePayload;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePrompt;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTarget;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTrace;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeWorkerRoute;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import org.junit.jupiter.api.Test;

class AiInvokeResultTest {

    @Test
    void toCandidateShouldKeepMarkdownResultPayloadForImageAnalysis() {
        AiInvokeCommand command = command(
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE,
                AiContentRef.ofNullable("SANCAI_ENTRY", 10L),
                new AiTargetObjectId(20L),
                new PromptVersionId(30L),
                true);

        AiInvokeResult result = new AiInvokeResult();
        result.setResultFormat("MARKDOWN");
        result.setResultPayload("image-analysis-body");
        result.setArtifactReferenceJson("{\"downloadPath\":\"artifact-1\"}");
        result.setFailureStage("WORKER_PROTOCOL_FAILURE");
        result.setErrorType("ERR");
        result.setErrorMessage("bad");
        result.setStreamCompleted(true);

        AiCandidate candidate = result.toCandidate(command, AiCallIdCodec.toDomain(100L));

        assertEquals(100L, candidate.getCallId().value());
        assertEquals(1L, candidate.getBatchId().value());
        assertEquals(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE, candidate.getCapability());
        assertEquals(AiContentRef.of("SANCAI_ENTRY", 10L), candidate.getContentRef());
        assertEquals(AiTargetObjectIdCodec.toDomain(20L), candidate.getTargetObjectId());
        assertEquals("MARKDOWN", candidate.getResultFormat());
        assertEquals("image-analysis-body", candidate.getResultPayload());
        assertEquals("{\"downloadPath\":\"artifact-1\"}", candidate.getArtifactReferenceJson());
        assertEquals("WORKER_PROTOCOL_FAILURE", candidate.getFailureStage());
        assertEquals("ERR", candidate.getErrorType());
        assertEquals("bad", candidate.getErrorMessage());
        assertEquals(30L, candidate.getPromptVersionId().value());
        assertEquals("model-a", candidate.getModelName().value());
    }

    @Test
    void toCandidateShouldKeepFailureSnapshot() {
        AiInvokeCommand command = command(
                AiBusinessCapability.CLASSICS_TRANSLATE, AiContentRef.ofNullable("ENTRY", 10L), null, null, false);

        AiInvokeResult result = new AiInvokeResult();
        result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.FAILED);
        result.setFailureStage("WORKER_RESULT");
        result.setErrorType("WORKER_PROTOCOL_FAILURE");
        result.setErrorMessage("bad");
        result.setResultFormat("TEXT");
        result.setResultPayload("bad-payload");

        AiCandidate candidate = result.toCandidate(command, AiCallIdCodec.toDomain(100L));

        assertEquals("WORKER_RESULT", candidate.getFailureStage());
        assertEquals("WORKER_PROTOCOL_FAILURE", candidate.getErrorType());
        assertEquals("bad", candidate.getErrorMessage());
        assertEquals("TEXT", candidate.getResultFormat());
        assertEquals("bad-payload", candidate.getResultPayload());
    }

    private AiInvokeCommand command(
            AiBusinessCapability capability,
            AiContentRef contentRef,
            AiTargetObjectId targetObjectId,
            PromptVersionId promptVersionId,
            boolean stream) {
        return new AiInvokeCommand(
                new AiInvokeContext(new AiBatchJobId(1L), null, capability),
                new AiInvokeWorkerRoute(null, null, null),
                new AiInvokeTarget(contentRef, targetObjectId),
                new AiInvokeModelConfig(null, null, null, AiModelName.of("model-a")),
                new AiInvokeTrace(null, null),
                new AiInvokePrompt(promptVersionId, null, null, null),
                new AiInvokePayload(null, null),
                new AiInvokeOptions(stream, false, null, false, false));
    }
}
