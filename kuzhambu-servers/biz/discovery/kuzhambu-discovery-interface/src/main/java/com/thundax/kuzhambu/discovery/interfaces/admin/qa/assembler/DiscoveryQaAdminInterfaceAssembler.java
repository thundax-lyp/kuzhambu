package com.thundax.kuzhambu.discovery.interfaces.admin.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response.DiscoveryQaAdminResponses;
import java.util.List;

public final class DiscoveryQaAdminInterfaceAssembler {

    private DiscoveryQaAdminInterfaceAssembler() {}

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
                .messages(toMessageResponses(result.getMessages()))
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
                .rewrittenQuestion(result.getRewrittenQuestion())
                .scope(result.getScope())
                .filtersJson(result.getFiltersJson())
                .expandedTermsJson(result.getExpandedTermsJson())
                .linkedEntitiesJson(result.getLinkedEntitiesJson())
                .candidateCount(result.getCandidateCount())
                .contextSnapshot(result.getContextSnapshot())
                .retrievedAt(result.getRetrievedAt())
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
