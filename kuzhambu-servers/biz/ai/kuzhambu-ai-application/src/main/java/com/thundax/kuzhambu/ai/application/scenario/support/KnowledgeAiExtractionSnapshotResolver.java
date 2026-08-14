package com.thundax.kuzhambu.ai.application.scenario.support;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeContext;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeModelConfig;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePayload;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePrompt;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTarget;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTrace;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeWorkerRoute;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver.ResolvedBusinessInvokeConfig;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeAiExtractionSnapshotResolver {

    private static final String DEFAULT_SCOPE = "knowledge";

    private final KnowledgeAiWorkerUsecaseResolver usecaseResolver;
    private final AiBusinessInvokeConfigResolver businessInvokeConfigResolver;

    public KnowledgeAiExtractionSnapshotResolver(
            KnowledgeAiWorkerUsecaseResolver usecaseResolver,
            AiBusinessInvokeConfigResolver businessInvokeConfigResolver) {
        this.usecaseResolver = usecaseResolver;
        this.businessInvokeConfigResolver = businessInvokeConfigResolver;
    }

    public KnowledgeAiExtractionSnapshot resolve(KnowledgeAiExtractionCommand command) {
        if (isResolved(command)) {
            return new KnowledgeAiExtractionSnapshot(command);
        }
        KnowledgeAiWorkerUsecaseSpec spec = usecaseResolver.resolve(command.taskType());
        AiInvokeCommand invokeCommand = toInvokeCommand(command, spec);
        ResolvedBusinessInvokeConfig resolved = businessInvokeConfigResolver.resolveConfig(invokeCommand);
        return new KnowledgeAiExtractionSnapshot(new KnowledgeAiExtractionCommand(
                command.batchId(),
                command.taskType(),
                scope(command),
                command.scopeJson(),
                command.sourceContentType(),
                command.sourceContentId(),
                command.requestedBy(),
                resolved.serviceId(),
                resolved.serviceRole(),
                resolved.modelId(),
                resolved.modelName(),
                resolved.promptVersionId(),
                command.requestId() == null ? RequestIdCodec.generate() : command.requestId(),
                command.traceId() == null ? TraceIdCodec.generate() : command.traceId(),
                resolved.promptMessagesJson(),
                resolved.promptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                resolved.outputSchemaJson(),
                command.forceJson(),
                command.locale()));
    }

    private AiInvokeCommand toInvokeCommand(KnowledgeAiExtractionCommand command, KnowledgeAiWorkerUsecaseSpec spec) {
        return new AiInvokeCommand(
                new AiInvokeContext(command.batchId(), scope(command), AiBusinessCapability.from(spec.capability())),
                new AiInvokeWorkerRoute(spec.workerCapability(), spec.operation(), spec.workerPath()),
                new AiInvokeTarget(
                        AiContentRef.ofNullable(command.sourceContentType(), command.sourceContentId()), null),
                new AiInvokeModelConfig(
                        command.serviceId(), command.serviceRole(), command.modelId(), command.modelName()),
                new AiInvokeTrace(
                        command.requestId() == null ? RequestIdCodec.generate() : command.requestId(),
                        command.traceId() == null ? TraceIdCodec.generate() : command.traceId()),
                new AiInvokePrompt(
                        command.promptVersionId(),
                        command.promptMessagesJson(),
                        command.promptVariablesJson(),
                        command.promptHash()),
                new AiInvokePayload(command.inputPayloadJson(), command.outputSchemaJson()),
                new AiInvokeOptions(false, command.forceJson(), command.locale(), false, true));
    }

    private boolean isResolved(KnowledgeAiExtractionCommand command) {
        return command != null
                && command.promptVersionId() != null
                && !isBlank(command.promptMessagesJson())
                && !isBlank(command.promptVariablesJson())
                && !isBlank(command.outputSchemaJson())
                && command.requestId() != null
                && command.traceId() != null;
    }

    private String scope(KnowledgeAiExtractionCommand command) {
        return isBlank(command.scopeType()) ? DEFAULT_SCOPE : command.scopeType();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
