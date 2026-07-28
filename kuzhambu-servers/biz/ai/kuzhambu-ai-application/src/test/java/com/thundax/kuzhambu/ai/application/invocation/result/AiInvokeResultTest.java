package com.thundax.kuzhambu.ai.application.invocation.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import org.junit.jupiter.api.Test;

class AiInvokeResultTest {

    @Test
    void toCandidateShouldKeepMarkdownResultPayloadForImageAnalysis() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setBatchId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId(1L));
        command.setCapability(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE);
        command.setContentRef(com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                "SANCAI_ENTRY", 10L));
        command.setTargetObjectId(
                new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId(20L));
        command.setPromptVersionId(new PromptVersionId(30L));
        command.setModelName(AiModelName.of("model-a"));
        command.setStream(true);

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
        AiInvokeCommand command = new AiInvokeCommand();
        command.setBatchId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId(1L));
        command.setCapability(AiBusinessCapability.CLASSICS_TRANSLATE);
        command.setContentRef(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable("ENTRY", 10L));
        command.setModelName(AiModelName.of("model-a"));

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
}
