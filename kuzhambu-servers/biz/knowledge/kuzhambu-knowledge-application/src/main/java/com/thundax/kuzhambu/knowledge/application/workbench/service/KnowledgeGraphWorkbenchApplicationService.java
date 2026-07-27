package com.thundax.kuzhambu.knowledge.application.workbench.service;

import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateApplyResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateSummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptDetailResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import java.util.List;

public interface KnowledgeGraphWorkbenchApplicationService {

    List<ManuscriptTreeNodeResult> listManuscriptTree(
            String sourceContentType, String parentKey, String keyword, String graphStatus);

    ManuscriptDetailResult getManuscript(String sourceContentType, Long sourceContentId);

    GraphExtractionTaskResult extractManuscript(
            String sourceContentType, Long sourceContentId, String taskType, Long requestedBy);

    CandidateSummaryResult getLatestCandidate(String sourceContentType, Long sourceContentId, String taskType);

    CandidateApplyResult applyCandidate(Long taskId);
}
