package com.thundax.kuzhambu.discovery.application.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatSource;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QaTraceAssembler {

    private static final String DEFAULT_PROVIDER = "unknown";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QaRetrievalTrace toDomain(
            ChatCompletionCommand command,
            QaSession session,
            Long messageId,
            String question,
            KnowledgeChatRequest providerRequest,
            KnowledgeChatResult chatResult,
            List<KnowledgeChatSource> sources,
            Long latencyMs,
            String failureReason) {
        return new QaRetrievalTrace(
                null,
                null,
                messageId,
                question,
                resolveProvider(session, command, chatResult),
                command == null ? null : command.getModel(),
                extractExternalKnowledgeItemIds(sources),
                chatResult == null ? null : chatResult.id(),
                resolveProviderRequestId(command, chatResult),
                latencyMs,
                failureReason,
                writeJson(traceRaw(providerRequest, chatResult)),
                null,
                null,
                null,
                null,
                new Date());
    }

    public QaRetrievalTrace toAiDomain(
            ChatCompletionCommand command,
            QaSession session,
            Long messageId,
            String question,
            DiscoveryAiFacadeRequest aiRequest,
            DiscoveryAiFacadeResponse aiResponse,
            List<QaSource> sources,
            Long latencyMs,
            String failureReason) {
        return new QaRetrievalTrace(
                null,
                null,
                messageId,
                question,
                "ai-discovery",
                aiRequest == null ? null : aiRequest.getModelName(),
                extractSourceBusinessIds(sources),
                aiResponse == null || aiResponse.getCallId() == null ? null : String.valueOf(aiResponse.getCallId()),
                resolveAiProviderRequestId(command, aiRequest),
                latencyMs,
                failureReason,
                writeJson(aiTraceRaw(aiRequest, aiResponse)),
                aiResponse == null ? null : aiResponse.getCallId(),
                aiResponse == null ? null : aiResponse.getStatus(),
                aiResponse == null ? null : aiResponse.getErrorType(),
                aiResponse == null ? null : aiResponse.getErrorMessage(),
                new Date());
    }

    public QaTraceResult toTraceResult(QaRetrievalTrace trace) {
        if (trace == null) {
            return null;
        }
        return new QaTraceResult(
                trace.getTraceId(),
                trace.getMessageId(),
                trace.getRawQuestion(),
                trace.getProvider(),
                trace.getExternalKnowledgeBaseId(),
                trace.getExternalKnowledgeItemIds(),
                trace.getExternalChatId(),
                trace.getProviderRequestId(),
                trace.getLatencyMs(),
                trace.getFailureReason(),
                trace.getRaw(),
                trace.getAiCallId(),
                trace.getAiStatus(),
                trace.getAiErrorType(),
                trace.getAiErrorMessage(),
                trace.getRetrievedAt());
    }

    private String resolveProvider(QaSession session, ChatCompletionCommand command, KnowledgeChatResult chatResult) {
        String configuredModel = command == null ? null : command.getModel();
        if (StringUtils.isNotBlank(configuredModel)) {
            return configuredModel;
        }
        if (session != null && StringUtils.isNotBlank(session.getKnowledgeBaseName())) {
            return session.getKnowledgeBaseName();
        }
        if (chatResult != null && StringUtils.isNotBlank(chatResult.model())) {
            return chatResult.model();
        }
        return DEFAULT_PROVIDER;
    }

    private String resolveProviderRequestId(ChatCompletionCommand command, KnowledgeChatResult chatResult) {
        if (chatResult != null && StringUtils.isNotBlank(chatResult.id())) {
            return chatResult.id();
        }
        if (command != null && StringUtils.isNotBlank(command.getTraceId())) {
            return command.getTraceId();
        }
        return null;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-30005", "discovery.qa.trace-json-build-failed", "QA trace json build failed", exception);
        }
    }

    private Map<String, Object> traceRaw(KnowledgeChatRequest providerRequest, KnowledgeChatResult chatResult) {
        Map<String, Object> raw = new LinkedHashMap<>();
        if (providerRequest != null) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", providerRequest.model());
            request.put("messages", providerRequest.messages());
            request.put("stream", providerRequest.stream());
            request.put("metadata", providerRequest.metadata());
            request.put("options", providerRequest.options());
            raw.put("providerRequest", request);
        }
        if (chatResult != null) {
            raw.put("providerResponse", chatResult.raw());
        }
        return raw.isEmpty() ? null : raw;
    }

    private Map<String, Object> aiTraceRaw(DiscoveryAiFacadeRequest aiRequest, DiscoveryAiFacadeResponse aiResponse) {
        Map<String, Object> raw = new LinkedHashMap<>();
        if (aiRequest != null) {
            raw.put("aiRequest", aiRequest);
        }
        if (aiResponse != null) {
            raw.put("aiResponse", aiResponse);
        }
        return raw.isEmpty() ? null : raw;
    }

    private String extractExternalKnowledgeItemIds(List<KnowledgeChatSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        return writeJson(sources.stream()
                .filter(source -> StringUtils.isNotBlank(source.sourceId()))
                .map(KnowledgeChatSource::sourceId)
                .toList());
    }

    private String extractSourceBusinessIds(List<QaSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        return writeJson(sources.stream()
                .filter(source -> StringUtils.isNotBlank(source.getSourceBusinessId()))
                .map(QaSource::getSourceBusinessId)
                .toList());
    }

    private String resolveAiProviderRequestId(ChatCompletionCommand command, DiscoveryAiFacadeRequest aiRequest) {
        if (aiRequest != null && StringUtils.isNotBlank(aiRequest.getRequestId())) {
            return aiRequest.getRequestId();
        }
        if (command != null && StringUtils.isNotBlank(command.getTraceId())) {
            return command.getTraceId();
        }
        return null;
    }
}
