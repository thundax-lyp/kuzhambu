package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionCandidatePreviewResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import org.springframework.stereotype.Component;

@Component
public class GraphTaskCandidateResolver {

    private static final String AI_CAPABILITY = "KNOWLEDGE_GRAPH_EXTRACT";

    private final AiFacade aiFacade;

    public GraphTaskCandidateResolver(AiFacade aiFacade) {
        this.aiFacade = aiFacade;
    }

    public GraphExtractionCandidatePreviewResult resolve(GraphExtractionTask task) {
        if (task == null || task.getCandidateId() == null) {
            return null;
        }
        AiCandidateFacadeDto candidate = aiFacade.getCandidate(GetAiCandidateFacadeRequest.builder()
                .candidateId(task.getCandidateId())
                .build());
        if (candidate == null) {
            throw candidateUnavailable();
        }
        if (!AI_CAPABILITY.equals(candidate.getCapability())
                || task.getContentRef() == null
                || !ContentRefCodec.toContentType(task.getContentRef()).equals(candidate.getContentType())
                || !ContentRefCodec.toValue(task.getContentRef()).equals(candidate.getContentId())) {
            throw candidateUnavailable();
        }
        return new GraphExtractionCandidatePreviewResult(
                candidate.getCandidateId(), candidate.getResultFormat(), candidate.getResultPayload());
    }

    private BizException candidateUnavailable() {
        return new BizException(
                "GRAPH_CANDIDATE_UNAVAILABLE",
                "graph.candidate.unavailable",
                "Graph extraction candidate is unavailable");
    }
}
