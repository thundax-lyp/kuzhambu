package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiWorkerInvocationApplicationServiceImpl implements AiWorkerInvocationApplicationService {

    private final AiInvocationRepository aiInvocationRepository;
    private final WorkerAiClient workerAiClient;

    public AiWorkerInvocationApplicationServiceImpl(
            AiInvocationRepository aiInvocationRepository, WorkerAiClient workerAiClient) {
        this.aiInvocationRepository = aiInvocationRepository;
        this.workerAiClient = workerAiClient;
    }

    @Override
    public AiInvokeResult invoke(AiInvokeCommand command) {
        validateCommand(command);
        command.setStream(false);
        AiCallRecord callRecord = command.toRunningCallRecord();
        Long callId = aiInvocationRepository.saveCallRecord(callRecord);
        callRecord.setCallId(callId);
        AiInvokeResult result;
        try {
            result = workerAiClient.invoke(command);
        } catch (RuntimeException ex) {
            result = AiInvokeResult.failed(
                    command.getRequestId(), command.getTraceId(), "WORKER_UNAVAILABLE", ex.getMessage());
        }
        return completeCall(command, callRecord, normalizeResult(command, result));
    }

    @Override
    public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        command.setStream(true);
        AiCallRecord callRecord = command.toRunningCallRecord();
        Long callId = aiInvocationRepository.saveCallRecord(callRecord);
        callRecord.setCallId(callId);
        AtomicReference<AiInvokeResult> completedResult = new AtomicReference<>();
        try {
            workerAiClient.stream(command, event -> handleStreamEvent(eventConsumer, completedResult, event));
        } catch (RuntimeException ex) {
            completedResult.compareAndSet(
                    null,
                    AiInvokeResult.failed(
                            command.getRequestId(), command.getTraceId(), "WORKER_UNAVAILABLE", ex.getMessage()));
        }
        AiInvokeResult result = completedResult.get();
        if (result == null) {
            result = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker stream ended without completed event");
        }
        return completeCall(command, callRecord, normalizeResult(command, result));
    }

    private void handleStreamEvent(
            Consumer<AiStreamEventResult> eventConsumer,
            AtomicReference<AiInvokeResult> completedResult,
            AiStreamEventResult event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
        if (event != null && event.isCompleted()) {
            completedResult.set(event.toInvokeResult());
        }
        if (event != null && event.isError()) {
            completedResult.compareAndSet(
                    null,
                    AiInvokeResult.failed(
                            event.getRequestId(), event.getTraceId(), event.getErrorType(), event.getErrorMessage()));
        }
    }

    private AiInvokeResult completeCall(AiInvokeCommand command, AiCallRecord callRecord, AiInvokeResult result) {
        Instant completedAt = Instant.now();
        if (result.isSucceeded()) {
            callRecord.markSucceeded(result.getUsage(), completedAt);
            callRecord.setWarningsJson(result.getWarningsJson());
            aiInvocationRepository.updateCallRecord(callRecord);
            if (command.isCreateCandidate()) {
                Long candidateId =
                        aiInvocationRepository.saveCandidate(result.toCandidate(command, callRecord.getCallId()));
                result.setCandidateId(candidateId);
            }
        } else {
            callRecord.markFailed(result.getErrorType(), result.getErrorMessage(), result.getUsage(), completedAt);
            aiInvocationRepository.updateCallRecord(callRecord);
        }
        result.setCallId(callRecord.getCallId());
        return result;
    }

    private AiInvokeResult normalizeResult(AiInvokeCommand command, AiInvokeResult result) {
        if (result == null) {
            return AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker returned empty result");
        }
        if (result.getRequestId() == null) {
            result.setRequestId(command.getRequestId());
        }
        if (result.getTraceId() == null) {
            result.setTraceId(command.getTraceId());
        }
        if (result.getCapability() == null) {
            result.setCapability(command.getCapability());
        }
        return result;
    }

    private void validateCommand(AiInvokeCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getCapability())
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || command.getModelId() == null
                || isBlank(command.getModelName())
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
