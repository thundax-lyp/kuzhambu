package com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response.DiscoveryQaAdminResponses;
import java.util.List;

public final class DiscoveryQaAdminInterfaceAssembler {

    private DiscoveryQaAdminInterfaceAssembler() {}

    private static final String DEFAULT_KNOWLEDGE_BASE_NAME = "kuzhambu-qa";

    public static DiscoveryQaAdminResponses.QaSessionDetailResponse toSessionDetailResponse(
            QaSessionDetailResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryQaAdminResponses.QaSessionDetailResponse.builder()
                .sessionId(result.getSessionId())
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
                .exportId(result.getExportId())
                .sessionId(result.getSessionId())
                .format(result.getFormat())
                .storageObjectId(result.getStorageObjectId())
                .exportStatus(result.getExportStatus())
                .failureReason(result.getFailureReason())
                .requestedAt(result.getRequestedAt())
                .completedAt(result.getCompletedAt())
                .filename(result.getFilename())
                .contentType(result.getContentType())
                .build();
    }

    public static List<DiscoveryQaAdminResponses.QaSourceResponse> toSourceResponses(List<QaSourceResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(result -> DiscoveryQaAdminResponses.QaSourceResponse.builder()
                        .sourceId(result.getSourceId())
                        .contentType(result.getContentType())
                        .contentId(result.getContentId())
                        .knowledgeBase(result.getKnowledgeBase())
                        .titleSnapshot(result.getTitleSnapshot())
                        .locationLabel(result.getLocationLabel())
                        .snippet(result.getSnippet())
                        .sourceRank(result.getSourceRank())
                        .score(result.getScore())
                        .sourceStatus(result.getSourceStatus())
                        .build())
                .toList();
    }

    public static DiscoveryQaAdminResponses.QaTraceResponse toTraceResponse(QaTraceResult result) {
        if (result == null) {
            return null;
        }
        return DiscoveryQaAdminResponses.QaTraceResponse.builder()
                .traceId(result.getTraceId())
                .messageId(result.getMessageId())
                .rawQuestion(result.getRawQuestion())
                .provider(result.getProvider())
                .externalKnowledgeBaseId(result.getExternalKnowledgeBaseId())
                .externalKnowledgeItemIds(result.getExternalKnowledgeItemIds())
                .externalChatId(result.getExternalChatId())
                .providerRequestId(result.getProviderRequestId())
                .latencyMs(result.getLatencyMs())
                .failureReason(result.getFailureReason())
                .raw(result.getRaw())
                .retrievedAt(result.getRetrievedAt())
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

    private static List<DiscoveryQaAdminResponses.QaMessageResponse> toMessageResponses(List<QaMessageResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(result -> DiscoveryQaAdminResponses.QaMessageResponse.builder()
                        .messageId(result.getMessageId())
                        .sessionId(result.getSessionId())
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
