package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public final class GraphExtractionResponses {

    private GraphExtractionResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskData(
            String id,
            GraphMaterialResponses.ContentRefData materialRef,
            String materialTitle,
            String categoryName,
            String volumeName,
            String lockVersion,
            String executionStatus,
            String disposition,
            String attemptNo,
            String progress,
            String currentStage,
            String candidateId,
            String resultSummary,
            String failureReason,
            String batchId,
            String regeneratedFromTaskId,
            String supersededByTaskId,
            String triggeredByTaskId,
            String requestedAt,
            String completedAt,
            String disposedAt,
            String purgeAfter) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskDeleteData(String deletedTaskId) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record StageData(
            String stageOrder,
            String stageName,
            String status,
            String progress,
            String inputSummaryJson,
            String outputSummaryJson,
            String failureReason,
            String startedAt,
            String completedAt) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record CandidatePreviewData(String candidateId, String resultFormat, String resultSummaryJson) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskDetailData(
            TaskData task,
            GraphMaterialResponses.SourceData source,
            GraphMaterialResponses.MaterialStatsData materialStats,
            List<StageData> stages,
            List<TaskData> relatedTasks,
            CandidatePreviewData candidate) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchResultData(String batchId, List<TaskData> materials) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record CandidateApplyData(TaskData task, GraphMaterialResponses.DetailData material) {}
}
