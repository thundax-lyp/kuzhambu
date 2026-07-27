package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateApplyResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateSummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptDetailResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.KnowledgeGraphExtractionInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.response.KnowledgeGraphWorkbenchResponses;
import java.util.List;

public final class KnowledgeGraphWorkbenchInterfaceAssembler {

    private KnowledgeGraphWorkbenchInterfaceAssembler() {}

    public static List<KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse> toTreeResponses(
            List<ManuscriptTreeNodeResult> results) {
        return results == null
                ? List.of()
                : results.stream()
                        .map(KnowledgeGraphWorkbenchInterfaceAssembler::toResponse)
                        .toList();
    }

    public static KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse toResponse(
            ManuscriptTreeNodeResult result) {
        return KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse.builder()
                .nodeKey(result == null ? null : result.getNodeKey())
                .parentKey(result == null ? null : result.getParentKey())
                .nodeType(result == null ? null : result.getNodeType())
                .title(result == null ? null : result.getTitle())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .sourcePath(result == null ? null : result.getSourcePath())
                .graphStatus(result == null ? null : result.getGraphStatus())
                .latestTaskId(result == null ? null : result.getLatestTaskId())
                .latestGraphVersionId(result == null ? null : result.getLatestGraphVersionId())
                .children(result == null ? List.of() : toTreeResponses(result.getChildren()))
                .build();
    }

    public static KnowledgeGraphWorkbenchResponses.ManuscriptDetailResponse toResponse(ManuscriptDetailResult result) {
        return KnowledgeGraphWorkbenchResponses.ManuscriptDetailResponse.builder()
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .title(result == null ? null : result.getTitle())
                .summary(result == null ? null : result.getSummary())
                .sourcePath(result == null ? null : result.getSourcePath())
                .currentVersionNo(result == null ? null : result.getCurrentVersionNo())
                .graphStatus(result == null ? null : result.getGraphStatus())
                .latestExtractionTask(toTaskResponse(result == null ? null : result.getLatestExtractionTask()))
                .latestGraphVersion(KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                        result == null ? null : result.getLatestGraphVersion()))
                .qualitySummary(toResponse(result == null ? null : result.getQualitySummary()))
                .build();
    }

    public static GraphExtractionResponses.TaskResponse toTaskResponse(GraphExtractionTaskResult result) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(result);
    }

    public static KnowledgeGraphWorkbenchResponses.CandidateSummaryResponse toResponse(CandidateSummaryResult result) {
        return KnowledgeGraphWorkbenchResponses.CandidateSummaryResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .aiCandidateId(result == null ? null : result.getAiCandidateId())
                .taskType(result == null ? null : result.getTaskType())
                .status(result == null ? null : result.getStatus())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .candidatePayloadJson(result == null ? null : result.getCandidatePayloadJson())
                .build();
    }

    public static KnowledgeGraphWorkbenchResponses.CandidateApplyResponse toResponse(CandidateApplyResult result) {
        return KnowledgeGraphWorkbenchResponses.CandidateApplyResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .graphStatus(result == null ? null : result.getGraphStatus())
                .build();
    }

    public static KnowledgeGraphWorkbenchResponses.QualitySummaryResponse toResponse(QualitySummaryResult result) {
        return KnowledgeGraphWorkbenchResponses.QualitySummaryResponse.builder()
                .entityCoverageRate(result == null ? null : result.getEntityCoverageRate())
                .relationAccuracyRate(result == null ? null : result.getRelationAccuracyRate())
                .completenessRate(result == null ? null : result.getCompletenessRate())
                .build();
    }
}
