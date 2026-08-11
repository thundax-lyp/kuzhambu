package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateApplyResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateSummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptDetailResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.KnowledgeGraphExtractionInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.response.KnowledgeGraphWorkbenchResponses;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgeGraphWorkbenchInterfaceAssembler {

    private KnowledgeGraphWorkbenchInterfaceAssembler() {}

    @NonNull
    public static List<KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse> toTreeResponses(
            @NonNull List<ManuscriptTreeNodeResult> results) {
        Objects.requireNonNull(results, "results must not be null");
        return results == null
                ? List.of()
                : results.stream()
                        .map(KnowledgeGraphWorkbenchInterfaceAssembler::toResponse)
                        .toList();
    }

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse toResponse(
            @NonNull ManuscriptTreeNodeResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.ManuscriptDetailResponse toResponse(
            @NonNull ManuscriptDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgeGraphWorkbenchResponses.ManuscriptDetailResponse.builder()
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .title(result == null ? null : result.getTitle())
                .summary(result == null ? null : result.getSummary())
                .sourcePath(result == null ? null : result.getSourcePath())
                .currentVersionNo(result == null ? null : result.getCurrentVersionNo())
                .graphStatus(result == null ? null : result.getGraphStatus())
                .latestExtractionTask(toOptionalTaskResponse(result == null ? null : result.getLatestExtractionTask()))
                .latestGraphVersion(
                        result == null || result.getLatestGraphVersion() == null
                                ? null
                                : KnowledgeGraphExtractionInterfaceAssembler.toResponse(result.getLatestGraphVersion()))
                .qualitySummary(toResponse(result == null ? null : result.getQualitySummary()))
                .build();
    }

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.TaskResponse toTaskResponse(
            @NonNull GraphExtractionTaskResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return toOptionalTaskResponse(result);
    }

    private static KnowledgeGraphWorkbenchResponses.TaskResponse toOptionalTaskResponse(
            GraphExtractionTaskResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgeGraphWorkbenchResponses.TaskResponse.builder()
                .task(KnowledgeGraphExtractionInterfaceAssembler.toResponse(result))
                .build();
    }

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.CandidateSummaryResponse toResponse(
            @NonNull CandidateSummaryResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgeGraphWorkbenchResponses.CandidateSummaryResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .aiCandidateId(result == null ? null : result.getAiCandidateId())
                .taskType(result == null ? null : result.getTaskType())
                .status(result == null ? null : result.getStatus())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .candidatePayloadJson(result == null ? null : result.getCandidatePayloadJson())
                .entities(
                        result == null
                                ? List.of()
                                : safeList(result.getEntities()).stream()
                                        .map(entity ->
                                                KnowledgeGraphWorkbenchResponses.CandidateEntityResponse.builder()
                                                        .name(entity.getName())
                                                        .entityType(entity.getEntityType())
                                                        .description(entity.getDescription())
                                                        .build())
                                        .toList())
                .relations(
                        result == null
                                ? List.of()
                                : safeList(result.getRelations()).stream()
                                        .map(relation ->
                                                KnowledgeGraphWorkbenchResponses.CandidateRelationResponse.builder()
                                                        .sourceName(relation.getSourceName())
                                                        .sourceType(relation.getSourceType())
                                                        .relationType(relation.getRelationType())
                                                        .targetName(relation.getTargetName())
                                                        .targetType(relation.getTargetType())
                                                        .evidence(relation.getEvidence())
                                                        .build())
                                        .toList())
                .warnings(result == null ? List.of() : safeList(result.getWarnings()))
                .build();
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.CandidateApplyResponse toResponse(
            @NonNull CandidateApplyResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgeGraphWorkbenchResponses.CandidateApplyResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .graphStatus(result == null ? null : result.getGraphStatus())
                .build();
    }

    @NonNull
    public static KnowledgeGraphWorkbenchResponses.QualitySummaryResponse toResponse(
            @NonNull QualitySummaryResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgeGraphWorkbenchResponses.QualitySummaryResponse.builder()
                .entityCoverageRate(result == null ? null : result.getEntityCoverageRate())
                .relationAccuracyRate(result == null ? null : result.getRelationAccuracyRate())
                .completenessRate(result == null ? null : result.getCompletenessRate())
                .build();
    }
}
