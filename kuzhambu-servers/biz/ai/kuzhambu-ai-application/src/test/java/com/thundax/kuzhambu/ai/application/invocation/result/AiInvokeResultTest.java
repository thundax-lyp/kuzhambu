package com.thundax.kuzhambu.ai.application.invocation.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import org.junit.jupiter.api.Test;

class AiInvokeResultTest {

    @Test
    void toCandidateShouldKeepMarkdownResultPayloadForImageAnalysis() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setBatchId(1L);
        command.setCapability("image_analysis");
        command.setContentType("SANCAI_ENTRY");
        command.setContentId(10L);
        command.setObjectId(20L);
        command.setPromptVersionId(30L);
        command.setModelName("model-a");
        command.setStream(true);

        AiInvokeResult result = new AiInvokeResult();
        result.setResultFormat("MARKDOWN");
        result.setResultPayload("image-analysis-body");
        result.setArtifactReferenceJson("{\"downloadPath\":\"artifact-1\"}");
        result.setFailureStage("WORKER_PROTOCOL_FAILURE");
        result.setErrorType("ERR");
        result.setErrorMessage("bad");
        result.setStreamCompleted(true);

        AiCandidate candidate = result.toCandidate(command, 100L);

        assertEquals(100L, candidate.getCallId());
        assertEquals(1L, candidate.getBatchId());
        assertEquals("image_analysis", candidate.getCapability());
        assertEquals("SANCAI_ENTRY", candidate.getContentType());
        assertEquals(10L, candidate.getContentId());
        assertEquals(20L, candidate.getObjectId());
        assertEquals("MARKDOWN", candidate.getResultFormat());
        assertEquals("image-analysis-body", candidate.getResultPayload());
        assertEquals("{\"downloadPath\":\"artifact-1\"}", candidate.getArtifactReferenceJson());
        assertEquals("WORKER_PROTOCOL_FAILURE", candidate.getFailureStage());
        assertEquals("ERR", candidate.getErrorType());
        assertEquals("bad", candidate.getErrorMessage());
        assertEquals(30L, candidate.getPromptVersionId());
        assertEquals("model-a", candidate.getModelName());
    }

    @Test
    void toCandidateShouldKeepFailureSnapshot() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setBatchId(1L);
        command.setCapability("translate");
        command.setContentType("ENTRY");
        command.setContentId(10L);
        command.setModelName("model-a");

        AiInvokeResult result = new AiInvokeResult();
        result.setStatus("FAILED");
        result.setFailureStage("WORKER_RESULT");
        result.setErrorType("WORKER_PROTOCOL_FAILURE");
        result.setErrorMessage("bad");
        result.setResultFormat("TEXT");
        result.setResultPayload("bad-payload");

        AiCandidate candidate = result.toCandidate(command, 100L);

        assertEquals("WORKER_RESULT", candidate.getFailureStage());
        assertEquals("WORKER_PROTOCOL_FAILURE", candidate.getErrorType());
        assertEquals("bad", candidate.getErrorMessage());
        assertEquals("TEXT", candidate.getResultFormat());
        assertEquals("bad-payload", candidate.getResultPayload());
    }
}
