package com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemQuery;
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

public final class DiscoveryQaAdminInterfaceAssembler {

    private DiscoveryQaAdminInterfaceAssembler() {}

    private static final String DEFAULT_KNOWLEDGE_BASE_NAME = "kuzhambu-qa";

    public static DiscoveryQaAdminResponses.QaSessionResponse toSessionResponse(QaSessionResult result) {
        if (result == null) {
            return null;
        }
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

    public static DiscoveryQaAdminResponses.QaSessionDetailResponse toSessionDetailResponse(
            QaSessionDetailResult result) {
        if (result == null) {
            return null;
        }
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

    public static DiscoveryQaAdminResponses.QaSessionExportResponse toSessionExportResponse(
            QaSessionExportResult result) {
        if (result == null) {
            return null;
        }
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

    public static DiscoveryQaAdminResponses.QaKnowledgeHealthResponse toHealthResponse(KnowledgeHealthResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryQaAdminResponses.QaKnowledgeHealthResponse.builder()
                .knowledgeBaseName(DEFAULT_KNOWLEDGE_BASE_NAME)
                .status(result.isAvailable() ? "AVAILABLE" : "UNAVAILABLE")
                .provider(result.getProvider())
                .checkedAt(System.currentTimeMillis())
                .failureReason(result.isAvailable() ? null : result.getMessage())
                .raw(result.getRaw())
                .build();
    }

    public static DiscoveryQaAdminResponses.QaSyncItemResponse toSyncItemResponse(KnowledgeSyncItemResult result) {
        if (result == null) {
            return null;
        }
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

    public static SyncKnowledgeContentCommand toSyncKnowledgeContentCommand(
            DiscoveryQaAdminRequests.KnowledgeSyncRequest request) {
        return new SyncKnowledgeContentCommand(
                request == null ? null : request.getContentType(),
                request == null ? null : request.getContentId(),
                request == null ? null : request.getCurrentVersionNo(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId());
    }

    public static KnowledgeSyncItemQuery toKnowledgeSyncItemQuery(
            DiscoveryQaAdminRequests.KnowledgeSyncPageRequest request) {
        if (request == null) {
            return new KnowledgeSyncItemQuery(null, null);
        }
        return new KnowledgeSyncItemQuery(request.getContentType(), request.getSyncStatus());
    }

    public static DeleteQaSessionCommand toDeleteQaSessionCommand(
            DiscoveryQaAdminRequests.QaSessionDeleteRequest request) {
        return new DeleteQaSessionCommand(
                request == null ? null : DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                null,
                null,
                true);
    }

    public static QaSessionQuery toQaSessionQuery(DiscoveryQaAdminRequests.QaSessionPageRequest request) {
        if (request == null) {
            return new QaSessionQuery(null, null, null);
        }
        return new QaSessionQuery(request.getTitle(), request.getOpenedAtStart(), request.getOpenedAtEnd());
    }

    public static ExportQaSessionCommand toExportQaSessionCommand(
            DiscoveryQaAdminRequests.QaSessionExportRequest request) {
        return new ExportQaSessionCommand(
                request == null ? null : DiscoveryInterfaceIdCodec.toLongValue(request.getSessionId()),
                request == null ? null : request.getRequesterUserId(),
                null,
                null,
                true,
                request == null ? null : request.getFormat());
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
