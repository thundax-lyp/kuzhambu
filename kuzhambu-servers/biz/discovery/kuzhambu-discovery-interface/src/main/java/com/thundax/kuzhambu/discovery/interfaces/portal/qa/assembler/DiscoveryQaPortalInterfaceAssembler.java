package com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;

public final class DiscoveryQaPortalInterfaceAssembler {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String UNAVAILABLE = "UNAVAILABLE";

    private DiscoveryQaPortalInterfaceAssembler() {}

    public static OpenQaSessionCommand toOpenSessionCommand(DiscoveryQaRequests.OpenSessionRequest request) {
        if (request == null) {
            return null;
        }
        return new OpenQaSessionCommand(
                request.getOwnerUserId(),
                request.getTitle(),
                request.getScope(),
                request.getContextMode(),
                request.getContextContentType(),
                request.getContextContentId(),
                request.getRequestId(),
                request.getTraceId());
    }

    public static ChatCompletionCommand toChatCompletionCommand(DiscoveryQaRequests.ChatCompletionsRequest request) {
        if (request == null) {
            return null;
        }
        return new ChatCompletionCommand(
                request.getSessionId(),
                request.getModel(),
                request.getMessages() == null
                        ? List.of()
                        : request.getMessages().stream()
                                .filter(Objects::nonNull)
                                .map(message ->
                                        new ChatCompletionCommand.ChatMessage(message.getRole(), message.getContent()))
                                .toList(),
                Boolean.TRUE.equals(request.getStream()),
                request.getMetadata(),
                request.getOptions(),
                request.getRequestId(),
                request.getTraceId());
    }

    public static DiscoveryQaResponses.OpenSessionResponse toOpenSessionResponse(QaSessionResult result) {
        if (result == null) {
            return null;
        }
        DiscoveryQaResponses.OpenSessionResponse response = new DiscoveryQaResponses.OpenSessionResponse();
        response.setSessionId(result.getSessionId());
        response.setOwnerUserId(result.getOwnerUserId());
        response.setTitle(result.getTitle());
        response.setScope(result.getScope());
        response.setContextMode(result.getContextMode());
        response.setContextContentType(result.getContextContentType());
        response.setContextContentId(result.getContextContentId());
        response.setStatus(result.getStatus());
        response.setOpenedAt(result.getOpenedAt());
        response.setLastMessageAt(result.getLastMessageAt());
        return response;
    }

    public static DiscoveryQaResponses.ChatCompletionsResponse toChatCompletionsResponse(ChatCompletionResult result) {
        if (result == null) {
            return null;
        }
        DiscoveryQaResponses.ChatCompletionsResponse response = new DiscoveryQaResponses.ChatCompletionsResponse();
        response.setSessionId(result.getSessionId());
        response.setQuestionMessageId(result.getQuestionMessageId());
        response.setAnswerMessageId(result.getAnswerMessageId());
        response.setQuestion(result.getQuestion());
        response.setAnswer(getFirstChoiceAnswer(result.getChoices()));
        response.setAnswerStatus(result.getAnswerStatus());
        response.setFailureReason(result.getFailureReason());
        response.setSources(toSourceResponses(result.getSources()));
        response.setChoices(
                result.getChoices() == null
                        ? List.of()
                        : result.getChoices().stream()
                                .filter(Objects::nonNull)
                                .map(DiscoveryQaPortalInterfaceAssembler::toChatCompletionChoice)
                                .toList());
        if (result.getUsage() != null) {
            DiscoveryQaResponses.ChatCompletionUsage usage = new DiscoveryQaResponses.ChatCompletionUsage();
            usage.setPromptTokens(result.getUsage().getPromptTokens());
            usage.setCompletionTokens(result.getUsage().getCompletionTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }
        response.setRaw(result.getRaw());
        return response;
    }

    private static DiscoveryQaResponses.ChatCompletionChoice toChatCompletionChoice(
            ChatCompletionResult.ChatCompletionChoice choice) {
        if (choice == null) {
            return null;
        }
        DiscoveryQaResponses.ChatCompletionChoice response = new DiscoveryQaResponses.ChatCompletionChoice();
        response.setIndex(choice.getIndex());
        response.setFinishReason(choice.getFinishReason());
        if (choice.getMessage() != null) {
            DiscoveryQaResponses.ChatCompletionMessage message = new DiscoveryQaResponses.ChatCompletionMessage();
            message.setRole(choice.getMessage().getRole());
            message.setContent(choice.getMessage().getContent());
            response.setMessage(message);
        }
        return response;
    }

    private static List<DiscoveryQaResponses.QaSourceResponse> toSourceResponses(
            List<ChatCompletionResult.ChatCompletionSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return IntStream.range(0, sources.size())
                .filter(i -> sources.get(i) != null)
                .mapToObj(i -> toQaSourceResponse(sources.get(i), i + 1))
                .toList();
    }

    private static DiscoveryQaResponses.QaSourceResponse toQaSourceResponse(
            ChatCompletionResult.ChatCompletionSource source, int rank) {
        if (source == null) {
            return null;
        }
        DiscoveryQaResponses.QaSourceResponse response = new DiscoveryQaResponses.QaSourceResponse();
        response.setSourceId(source.getSourceId());
        response.setContentType(source.getContentType());
        response.setContentId(source.getContentId());
        response.setKnowledgeBase(source.getKnowledgeBase());
        response.setTitleSnapshot(source.getTitle());
        response.setSnippet(source.getSnippet());
        response.setScore(toBigDecimal(source.getScore()));
        response.setSourceStatus(toSourceStatus(source));
        response.setSourceRank(rank);
        response.setLocationLabel(sourceLabel(source.getRaw()));
        return response;
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static String sourceLabel(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        Object locationLabel = raw.get("locationLabel");
        return locationLabel == null ? null : locationLabel.toString();
    }

    private static String toSourceStatus(ChatCompletionResult.ChatCompletionSource source) {
        return StringUtils.isNotBlank(source.getSourceId())
                        && StringUtils.isNotBlank(source.getContentType())
                        && StringUtils.isNotBlank(source.getContentId())
                        && StringUtils.isNotBlank(source.getTitle())
                ? AVAILABLE
                : UNAVAILABLE;
    }

    private static String getFirstChoiceAnswer(List<ChatCompletionResult.ChatCompletionChoice> choices) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        for (ChatCompletionResult.ChatCompletionChoice choice : choices) {
            if (choice != null && choice.getMessage() != null) {
                return choice.getMessage().getContent();
            }
        }
        return null;
    }
}
