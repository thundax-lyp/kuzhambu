package com.thundax.kuzhambu.discovery.application.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QaTraceAssembler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QaRetrievalTrace toDomain(
            AskQuestionCommand command,
            QaSession session,
            QueryUnderstandingResult understandingResult,
            QaContextAssembler.QaContext qaContext,
            Long messageId) {
        return new QaRetrievalTrace(
                null,
                null,
                messageId,
                command.getQuestion(),
                qaContext.rewrittenQuestion(),
                session == null ? null : session.getScope(),
                writeJson(buildFilters(command, session, qaContext)),
                writeJson(qaContext.expandedTerms()),
                writeJson(qaContext.recognizedEntities()),
                qaContext.candidateCount(),
                qaContext.contextSnapshotJson(),
                new java.util.Date());
    }

    public QaAnswerResult.TraceSummaryResult toTraceSummary(
            QaRetrievalTrace trace,
            QueryUnderstandingResult understandingResult,
            QaContextAssembler.QaContext qaContext) {
        return new QaAnswerResult.TraceSummaryResult(
                trace.getTraceId(),
                understandingResult == null
                        ? qaContext.rewrittenQuestion()
                        : understandingResult.getRewrittenQueryText(),
                trace.getCandidateCount(),
                trace.getExpandedTermsJson(),
                trace.getLinkedEntitiesJson());
    }

    public QaTraceResult toTraceResult(QaRetrievalTrace trace) {
        if (trace == null) {
            return null;
        }
        return new QaTraceResult(
                trace.getTraceId(),
                trace.getMessageId(),
                trace.getRawQuestion(),
                trace.getRewrittenQuestion(),
                trace.getScope(),
                trace.getFiltersJson(),
                trace.getExpandedTermsJson(),
                trace.getLinkedEntitiesJson(),
                trace.getCandidateCount(),
                trace.getContextSnapshot(),
                trace.getRetrievedAt());
    }

    private Map<String, Object> buildFilters(
            AskQuestionCommand command, QaSession session, QaContextAssembler.QaContext qaContext) {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("sessionId", command.getSessionId());
        filters.put("scope", session == null ? null : session.getScope());
        filters.put("contextMode", session == null ? null : session.getContextMode());
        filters.put("operatorType", command.getOperatorType());
        filters.put("operatorId", command.getOperatorId());
        filters.put("sourceCount", qaContext.candidateCount());
        return filters;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-30005", "discovery.qa.trace-json-build-failed", "QA trace json build failed", exception);
        }
    }
}
