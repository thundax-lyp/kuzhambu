package com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.PortalQaSessionDetailQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.PortalQaSessionQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DiscoveryQaPortalInterfaceAssembler {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String UNAVAILABLE = "UNAVAILABLE";
    private static final String OWNER_TYPE_USER = "USER";
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private DiscoveryQaPortalInterfaceAssembler() {}

    public static @NonNull OpenQaSessionCommand toOpenSessionCommand(
            @NonNull DiscoveryQaRequests.OpenSessionRequest request) {
        Objects.requireNonNull(request, "request");
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

    public static @NonNull ChatCompletionCommand toChatCompletionCommand(
            @NonNull DiscoveryQaRequests.ChatCompletionsRequest request) {
        Objects.requireNonNull(request, "request");
        return toChatCompletionCommand(request, Boolean.TRUE.equals(request.getStream()));
    }

    public static @NonNull ChatCompletionCommand toChatCompletionCommand(
            @NonNull DiscoveryQaRequests.ChatCompletionsRequest request, boolean stream) {
        Objects.requireNonNull(request, "request");
        return new ChatCompletionCommand(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                request.getModel(),
                request.getMessages() == null
                        ? List.of()
                        : request.getMessages().stream()
                                .filter(Objects::nonNull)
                                .map(message -> new ChatCompletionMessage(message.getRole(), message.getContent()))
                                .toList(),
                stream,
                request.getMetadata(),
                request.getOptions(),
                request.getRequestId(),
                request.getTraceId());
    }

    public static @NonNull DeleteQaSessionCommand toDeleteSessionCommand(
            @NonNull DiscoveryQaRequests.QaSessionDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new DeleteQaSessionCommand(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                ownerType(),
                ownerId(request.getOwnerUserId()),
                false);
    }

    public static @NonNull ExportQaSessionCommand toExportSessionCommand(
            @NonNull DiscoveryQaRequests.QaSessionExportRequest request) {
        Objects.requireNonNull(request, "request");
        return new ExportQaSessionCommand(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                request.getOwnerUserId(),
                ownerType(),
                ownerId(request.getOwnerUserId()),
                false,
                request.getFormat());
    }

    public static @NonNull PortalQaSessionQuery toSessionQuery(
            @NonNull DiscoveryQaRequests.QaSessionPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new PortalQaSessionQuery(ownerType(), ownerId(request.getOwnerUserId()));
    }

    public static @NonNull PageQuery toPageQuery(@NonNull DiscoveryQaRequests.QaSessionPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new PageQuery(pageNo(request), limit(request));
    }

    public static @NonNull PortalQaSessionDetailQuery toSessionDetailQuery(
            @NonNull DiscoveryQaRequests.QaSessionGetRequest request) {
        Objects.requireNonNull(request, "request");
        return new PortalQaSessionDetailQuery(
                ownerType(),
                ownerId(request.getOwnerUserId()),
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()));
    }

    private static String ownerType() {
        return OWNER_TYPE_USER;
    }

    private static String ownerId(Long ownerUserId) {
        return ownerUserId == null ? null : String.valueOf(ownerUserId);
    }

    private static Integer limit(DiscoveryQaRequests.QaSessionPageRequest request) {
        if (request.getLimit() != null) {
            return request.getLimit();
        }
        if (request.getPageSize() != null) {
            return request.getPageSize();
        }
        return DEFAULT_PAGE_SIZE;
    }

    private static Integer pageNo(DiscoveryQaRequests.QaSessionPageRequest request) {
        if (request.getPageNo() == null) {
            return DEFAULT_PAGE_NO;
        }
        return request.getPageNo();
    }

    public static @NonNull DiscoveryQaResponses.OpenSessionResponse toOpenSessionResponse(
            @NonNull QaSessionResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaResponses.OpenSessionResponse.builder()
                .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                .ownerUserId(result.getOwnerUserId())
                .title(result.getTitle())
                .scope(result.getScope())
                .contextMode(result.getContextMode())
                .contextContentType(result.getContextContentType())
                .contextContentId(result.getContextContentId())
                .status(result.getStatus())
                .openedAt(result.getOpenedAt())
                .lastMessageAt(result.getLastMessageAt())
                .build();
    }

    public static @NonNull PageResponse<DiscoveryQaResponses.QaSessionResponse> toSessionPageResponse(
            @NonNull List<QaSessionResult> results, @NonNull DiscoveryQaRequests.QaSessionPageRequest request) {
        Objects.requireNonNull(results, "results");
        Objects.requireNonNull(request, "request");
        int pageNo = pageNo(request);
        int pageSize = limit(request);
        PageResponse<DiscoveryQaResponses.QaSessionResponse> response = new PageResponse<>();
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setCount(results.size());
        response.setTotalPage(results.isEmpty() ? 0 : 1);
        response.setRecords(results.stream()
                .filter(Objects::nonNull)
                .map(DiscoveryQaPortalInterfaceAssembler::toSessionResponse)
                .toList());
        return response;
    }

    public static @NonNull DiscoveryQaResponses.QaSessionResponse toSessionResponse(@NonNull QaSessionResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaResponses.QaSessionResponse.builder()
                .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                .ownerUserId(result.getOwnerUserId())
                .title(result.getTitle())
                .scope(result.getScope())
                .contextMode(result.getContextMode())
                .contextContentType(result.getContextContentType())
                .contextContentId(result.getContextContentId())
                .status(result.getStatus())
                .openedAt(result.getOpenedAt())
                .lastMessageAt(result.getLastMessageAt())
                .build();
    }

    public static @NonNull DiscoveryQaResponses.QaSessionDetailResponse toSessionDetailResponse(
            @NonNull QaSessionDetailResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaResponses.QaSessionDetailResponse.detailBuilder()
                .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                .ownerUserId(result.getOwnerUserId())
                .title(result.getTitle())
                .scope(result.getScope())
                .contextMode(result.getContextMode())
                .contextContentType(result.getContextContentType())
                .contextContentId(result.getContextContentId())
                .status(result.getStatus())
                .openedAt(result.getOpenedAt())
                .lastMessageAt(result.getLastMessageAt())
                .messages(
                        result.getMessages() == null
                                ? List.of()
                                : result.getMessages().stream()
                                        .filter(Objects::nonNull)
                                        .map(DiscoveryQaPortalInterfaceAssembler::toMessageResponse)
                                        .toList())
                .build();
    }

    public static @NonNull DiscoveryQaResponses.QaSessionExportResponse toSessionExportResponse(
            @NonNull QaSessionExportResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaResponses.QaSessionExportResponse.builder()
                .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                .sessionId(DiscoveryInterfaceIdCodec.toStringValue(result.getSessionId()))
                .format(result.getFormat())
                .storageObjectId(DiscoveryInterfaceIdCodec.toStringValue(result.getStorageObjectId()))
                .exportStatus(result.getExportStatus())
                .failureReason(result.getFailureReason())
                .requestedAt(result.getRequestedAt())
                .completedAt(result.getCompletedAt())
                .filename(result.getFilename())
                .contentType(result.getContentType())
                .build();
    }

    public static @NonNull DiscoveryQaResponses.ChatCompletionsResponse toChatCompletionsResponse(
            @NonNull ChatCompletionResult result) {
        Objects.requireNonNull(result, "result");
        DiscoveryQaResponses.ChatCompletionUsage usage = null;
        if (result.getUsage() != null) {
            usage = DiscoveryQaResponses.ChatCompletionUsage.builder()
                    .promptTokens(result.getUsage().getPromptTokens())
                    .completionTokens(result.getUsage().getCompletionTokens())
                    .totalTokens(result.getUsage().getTotalTokens())
                    .build();
        }
        return DiscoveryQaResponses.ChatCompletionsResponse.builder()
                .sessionId(DiscoveryInterfaceIdCodec.toStringValue(result.getSessionId()))
                .questionMessageId(DiscoveryInterfaceIdCodec.toStringValue(result.getQuestionMessageId()))
                .answerMessageId(DiscoveryInterfaceIdCodec.toStringValue(result.getAnswerMessageId()))
                .question(result.getQuestion())
                .answer(getFirstChoiceAnswer(result.getChoices()))
                .answerStatus(result.getAnswerStatus())
                .failureReason(result.getFailureReason())
                .sources(toSourceResponses(result.getSources()))
                .choices(
                        result.getChoices() == null
                                ? List.of()
                                : result.getChoices().stream()
                                        .filter(Objects::nonNull)
                                        .map(DiscoveryQaPortalInterfaceAssembler::toChatCompletionChoice)
                                        .toList())
                .usage(usage)
                .raw(result.getRaw())
                .build();
    }

    private static DiscoveryQaResponses.QaMessageResponse toMessageResponse(QaMessageResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryQaResponses.QaMessageResponse.builder()
                .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                .sessionId(DiscoveryInterfaceIdCodec.toStringValue(result.getSessionId()))
                .role(result.getRole())
                .content(result.getContent())
                .messageStatus(result.getMessageStatus())
                .contextTurnCount(result.getContextTurnCount())
                .failureReason(result.getFailureReason())
                .sentAt(toTimestamp(result.getSentAt()))
                .answeredAt(toTimestamp(result.getAnsweredAt()))
                .build();
    }

    private static Long toTimestamp(Instant date) {
        return date == null ? null : date.toEpochMilli();
    }

    private static DiscoveryQaResponses.ChatCompletionChoice toChatCompletionChoice(
            ChatCompletionResult.ChatCompletionChoice choice) {
        if (choice == null) {
            return null;
        }
        return DiscoveryQaResponses.ChatCompletionChoice.builder()
                .index(choice.getIndex())
                .finishReason(choice.getFinishReason())
                .message(
                        choice.getMessage() == null
                                ? null
                                : DiscoveryQaResponses.ChatCompletionMessage.builder()
                                        .role(choice.getMessage().getRole())
                                        .content(choice.getMessage().getContent())
                                        .build())
                .build();
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
        return DiscoveryQaResponses.QaSourceResponse.builder()
                .sourceId(source.getSourceId())
                .contentType(source.getContentType())
                .contentId(source.getContentId())
                .knowledgeBase(source.getKnowledgeBase())
                .titleSnapshot(source.getTitle())
                .snippet(source.getSnippet())
                .score(toBigDecimal(source.getScore()))
                .sourceStatus(toSourceStatus(source))
                .sourceRank(rank)
                .locationLabel(sourceLabel(source.getRaw()))
                .build();
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
