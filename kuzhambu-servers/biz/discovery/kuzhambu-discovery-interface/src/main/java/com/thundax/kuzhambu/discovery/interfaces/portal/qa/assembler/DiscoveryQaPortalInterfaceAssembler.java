package com.thundax.kuzhambu.discovery.interfaces.portal.qa.assembler;

import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response.DiscoveryQaResponses;
import java.util.List;
import java.util.Objects;

public final class DiscoveryQaPortalInterfaceAssembler {

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

    public static AskQuestionCommand toAskQuestionCommand(DiscoveryQaRequests.AskQuestionRequest request) {
        if (request == null) {
            return null;
        }
        return new AskQuestionCommand(
                request.getSessionId(),
                request.getQuestion(),
                request.getContextTurnCount(),
                request.getOperatorType(),
                request.getOperatorId(),
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

    public static DiscoveryQaResponses.AskQuestionResponse toAskQuestionResponse(QaAnswerResult result) {
        if (result == null) {
            return null;
        }
        DiscoveryQaResponses.AskQuestionResponse response = new DiscoveryQaResponses.AskQuestionResponse();
        response.setSessionId(result.getSessionId());
        response.setQuestionMessageId(result.getQuestionMessageId());
        response.setAnswerMessageId(result.getAnswerMessageId());
        response.setQuestion(result.getQuestion());
        response.setAnswer(result.getAnswer());
        response.setAnswerStatus(result.getAnswerStatus());
        response.setFailureReason(result.getFailureReason());
        response.setSources(
                result.getSources() == null
                        ? List.of()
                        : result.getSources().stream()
                                .filter(Objects::nonNull)
                                .map(source -> {
                                    DiscoveryQaResponses.QaSourceResponse item =
                                            new DiscoveryQaResponses.QaSourceResponse();
                                    item.setSourceId(source.getSourceId());
                                    item.setContentType(source.getContentType());
                                    item.setContentId(source.getContentId());
                                    item.setKnowledgeBase(source.getKnowledgeBase());
                                    item.setTitleSnapshot(source.getTitleSnapshot());
                                    item.setLocationLabel(source.getLocationLabel());
                                    item.setSnippet(source.getSnippet());
                                    item.setSourceRank(source.getSourceRank());
                                    item.setScore(source.getScore());
                                    item.setSourceStatus(source.getSourceStatus());
                                    return item;
                                })
                                .toList());
        if (result.getTraceSummary() != null) {
            DiscoveryQaResponses.QaTraceSummaryResponse traceSummary =
                    new DiscoveryQaResponses.QaTraceSummaryResponse();
            traceSummary.setTraceId(result.getTraceSummary().getTraceId());
            traceSummary.setRewrittenQuestion(result.getTraceSummary().getRewrittenQuestion());
            traceSummary.setCandidateCount(result.getTraceSummary().getCandidateCount());
            traceSummary.setExpandedTermsJson(result.getTraceSummary().getExpandedTermsJson());
            traceSummary.setLinkedEntitiesJson(result.getTraceSummary().getLinkedEntitiesJson());
            response.setTraceSummary(traceSummary);
        }
        return response;
    }
}
