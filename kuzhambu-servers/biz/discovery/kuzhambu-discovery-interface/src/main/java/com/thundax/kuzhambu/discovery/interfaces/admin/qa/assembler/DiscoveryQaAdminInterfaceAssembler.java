package com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionDetailQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response.DiscoveryQaAdminResponses;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class DiscoveryQaAdminInterfaceAssembler {

    private DiscoveryQaAdminInterfaceAssembler() {}

    private static final String DEFAULT_KNOWLEDGE_BASE_NAME = "kuzhambu-qa";

    @NonNull
    public static QaSessionDetailQuery toSessionDetailQuery(
            @NonNull DiscoveryQaAdminRequests.QaSessionGetRequest request) {
        Objects.requireNonNull(request, "request");
        return new QaSessionDetailQuery(DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()));
    }

    @NonNull
    public static DiscoveryQaAdminResponses.QaSessionResponse toSessionResponse(@NonNull QaSessionResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaAdminResponses.QaSessionResponse.builder()
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
                .removedAt(result.getRemovedAt())
                .build();
    }

    @NonNull
    public static DiscoveryQaAdminResponses.QaSessionDetailResponse toSessionDetailResponse(
            @NonNull QaSessionDetailResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaAdminResponses.QaSessionDetailResponse.builder()
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
                .removedAt(result.getRemovedAt())
                .messages(toMessageResponses(result.getMessages()))
                .build();
    }

    @NonNull
    public static DiscoveryQaAdminResponses.QaSessionExportResponse toSessionExportResponse(
            @NonNull QaSessionExportResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaAdminResponses.QaSessionExportResponse.builder()
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

    @NonNull
    public static DiscoveryQaAdminResponses.QaKnowledgeHealthResponse toHealthResponse(
            @NonNull KnowledgeHealthResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaAdminResponses.QaKnowledgeHealthResponse.builder()
                .knowledgeBaseName(DEFAULT_KNOWLEDGE_BASE_NAME)
                .status(result.isAvailable() ? "AVAILABLE" : "UNAVAILABLE")
                .provider(result.getProvider())
                .checkedAt(System.currentTimeMillis())
                .failureReason(result.isAvailable() ? null : result.getMessage())
                .raw(result.getRaw())
                .build();
    }

    @NonNull
    public static DiscoveryQaAdminResponses.QaSyncItemResponse toSyncItemResponse(
            @NonNull KnowledgeSyncItemResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoveryQaAdminResponses.QaSyncItemResponse.builder()
                .sourceId(result.getSourceId())
                .contentType(result.getContentType())
                .contentId(result.getContentId())
                .title(result.getTitle())
                .knowledgeBaseName(result.getKnowledgeBaseName())
                .currentVersionNo(result.getCurrentVersionNo())
                .knowledgeRevision(result.getKnowledgeRevision())
                .provider(result.getProvider())
                .externalKnowledgeBaseId(result.getExternalKnowledgeBaseId())
                .externalKnowledgeItemId(result.getExternalKnowledgeItemId())
                .syncStatus(result.getSyncStatus())
                .failureReason(result.getFailureReason())
                .syncedAt(result.getSyncedAt())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }

    @NonNull
    public static SyncKnowledgeContentCommand toSyncKnowledgeContentCommand(
            @NonNull DiscoveryQaAdminRequests.KnowledgeSyncRequest request) {
        Objects.requireNonNull(request, "request");
        return new SyncKnowledgeContentCommand(
                request.getContentType(),
                request.getContentId(),
                request.getCurrentVersionNo(),
                request.getRequestId(),
                request.getTraceId());
    }

    @NonNull
    public static KnowledgeSyncItemQuery toKnowledgeSyncItemQuery(
            @NonNull DiscoveryQaAdminRequests.KnowledgeSyncPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new KnowledgeSyncItemQuery(request.getContentType(), request.getSyncStatus());
    }

    @NonNull
    public static DeleteQaSessionCommand toDeleteQaSessionCommand(
            @NonNull DiscoveryQaAdminRequests.QaSessionDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new DeleteQaSessionCommand(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()), null, null, true);
    }

    @NonNull
    public static QaSessionQuery toQaSessionQuery(@NonNull DiscoveryQaAdminRequests.QaSessionPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new QaSessionQuery(request.getTitle(), request.getOpenedAtStart(), request.getOpenedAtEnd());
    }

    @NonNull
    public static ExportQaSessionCommand toExportQaSessionCommand(
            @NonNull DiscoveryQaAdminRequests.QaSessionExportRequest request) {
        Objects.requireNonNull(request, "request");
        return new ExportQaSessionCommand(
                DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                request.getRequesterUserId(),
                null,
                null,
                true,
                request.getFormat());
    }

    private static List<DiscoveryQaAdminResponses.QaMessageResponse> toMessageResponses(List<QaMessageResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(result -> DiscoveryQaAdminResponses.QaMessageResponse.builder()
                        .id(DiscoveryInterfaceIdCodec.toStringValue(result.getId()))
                        .sessionId(DiscoveryInterfaceIdCodec.toStringValue(result.getSessionId()))
                        .role(result.getRole())
                        .content(result.getContent())
                        .messageStatus(result.getMessageStatus())
                        .contextTurnCount(result.getContextTurnCount())
                        .failureReason(result.getFailureReason())
                        .sentAt(result.getSentAt())
                        .answeredAt(result.getAnsweredAt())
                        .build())
                .toList();
    }
}
