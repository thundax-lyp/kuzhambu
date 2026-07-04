package com.thundax.kuzhambu.discovery.application.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatSource;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import java.util.Date;
import java.util.List;
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
                chatResult == null ? null : writeJson(chatResult.raw()),
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

    private String extractExternalKnowledgeItemIds(List<KnowledgeChatSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        return writeJson(sources.stream()
                .filter(source -> StringUtils.isNotBlank(source.sourceId()))
                .map(KnowledgeChatSource::sourceId)
                .toList());
    }
}
