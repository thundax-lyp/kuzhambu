package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCancelCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateDiscardCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRegenerateCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialApplyMode;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionDecisionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionPrecheckCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionTaskRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialImportCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationConflictDecision;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialDeletionChangeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialDeletionTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialImportQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialTreeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedAdjacencyQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskDetailQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionCandidatePreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionStageResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceOperationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialChangeImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialImportPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialPageResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedAdjacencyResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedEdgeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialDeletionChangeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialDeletionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchActivity;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphDeletionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublishedRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphDeletionResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphMaterialResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublicationResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublishedResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.lang.NonNull;

/** 管理端图谱 HTTP 协议转换。 */
public final class GraphInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GraphInterfaceAssembler() {}

    @NonNull
    public static ContentRef toContentRef(@NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        GraphMaterialRequests.ContentRefRequest effective =
                request.getContentRef() == null ? request : request.getContentRef();
        if (effective.getContentType() == null
                || effective.getContentType().isBlank()
                || effective.getContentRefId() == null
                || effective.getContentRefId().isBlank()) {
            throw new ApiException("GRAPH_CONTENT_REF_REQUIRED");
        }
        return new ContentRef(effective.getContentType(), Long.valueOf(effective.getContentRefId()));
    }

    @NonNull
    public static GraphMaterialQuery toQuery(@NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        return toQueryInternal(request, null);
    }

    @NonNull
    public static GraphMaterialQuery toQuery(
            @NonNull GraphMaterialRequests.ContentRefRequest request, @NonNull String subjectId) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(subjectId, "subjectId");
        return toQueryInternal(request, subjectId);
    }

    private static GraphMaterialQuery toQueryInternal(
            GraphMaterialRequests.ContentRefRequest request, String subjectId) {
        return new GraphMaterialQuery(subjectId, toContentRef(request));
    }

    @NonNull
    public static GraphMaterialListQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialPageRequest request, @NonNull String subjectId) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(subjectId, "subjectId");
        return new GraphMaterialListQuery(
                subjectId,
                request.getKeyword(),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.valueOf(
                                request.getStatus()),
                request.getContentType(),
                request.getCategoryCode(),
                request.getVolumeCode(),
                request.getTaskExecutionStatus(),
                request.getTaskDisposition());
    }

    @NonNull
    public static GraphMaterialTreeQuery toQuery(@NonNull GraphMaterialRequests.MaterialTreeRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialTreeQuery(request.getParentId());
    }

    @NonNull
    public static GraphMaterialResponses.ContentRefData toContentRefData(@NonNull ContentRef value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ContentRefData(value.getContentType(), String.valueOf(value.getContentId()));
    }

    private static GraphMaterialResponses.ContentRefData toNullableContentRefData(ContentRef value) {
        return value == null ? null : toContentRefData(value);
    }

    @NonNull
    public static GraphWorkbenchResponses.ActivityData toActivityData(@NonNull GraphWorkbenchActivity value) {
        Objects.requireNonNull(value, "value");
        return new GraphWorkbenchResponses.ActivityData(
                value.type(),
                toNullableContentRefData(value.contentRef()),
                value.occurredAt() == null
                        ? null
                        : String.valueOf(value.occurredAt().toEpochMilli()),
                value.summary());
    }

    @NonNull
    public static GraphMaterialResponses.MaterialData toMaterialData(
            @NonNull com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.MaterialData(
                value.getContentRef() == null
                        ? null
                        : String.valueOf(value.getContentRef().getContentId()),
                toNullableContentRefData(value.getContentRef()),
                value.getContentTitleSnapshot(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()),
                value.getPublishedAt() == null
                        ? null
                        : String.valueOf(value.getPublishedAt().toEpochMilli()),
                value.getStatus() == com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.FAILED
                        ? value.getFailureReason()
                        : null,
                value.getStatus() == com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus.FAILED
                        ? value.getFailedOperation()
                        : null);
    }

    @NonNull
    public static GraphMaterialResponses.MaterialData toMaterialData(@NonNull GraphMaterialPageResult value) {
        Objects.requireNonNull(value, "value");
        return toMaterialData(Objects.requireNonNull(value.material(), "material"));
    }

    private static GraphMaterialResponses.MaterialData toNullableMaterialData(GraphMaterialPageResult value) {
        return value == null || value.material() == null ? null : toMaterialData(value.material());
    }

    private static GraphMaterialResponses.SourceData toNullableSourceData(GraphMaterialPageResult value) {
        return value == null ? null : toNullableSourceData(value.source());
    }

    private static GraphMaterialResponses.SourceData toNullableSourceData(
            com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialSourceResult value) {
        if (value == null) {
            return null;
        }
        return new GraphMaterialResponses.SourceData(
                toNullableContentRefData(value.contentRef()),
                value.title(),
                value.summary(),
                value.contentType(),
                value.categoryCode(),
                value.categoryName(),
                value.volumeCode(),
                value.volumeName(),
                value.graphable());
    }

    private static GraphMaterialResponses.MaterialStatsData toNullableMaterialStatsData(GraphMaterialStats value) {
        if (value == null) {
            return null;
        }
        return new GraphMaterialResponses.MaterialStatsData(
                string(value.getMaterialId()),
                String.valueOf(value.getDraftNodeCount()),
                String.valueOf(value.getDraftEdgeCount()),
                String.valueOf(value.getPublishedNodeCount()),
                String.valueOf(value.getPublishedEdgeCount()),
                String.valueOf(value.getActiveTaskCount()),
                String.valueOf(value.getPendingReviewTaskCount()),
                String.valueOf(value.getFailedTaskCount()),
                String.valueOf(value.getStatsRevision()),
                instant(value.getCalculatedAt()));
    }

    @NonNull
    public static GraphMaterialResponses.MaterialPageData toMaterialPageData(@NonNull GraphMaterialPageResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.MaterialPageData(
                toNullableSourceData(value.source()),
                toNullableMaterialData(value),
                toNullableMaterialStatsData(value.materialStats()),
                toNullableTaskData(value.latestTask()));
    }

    @NonNull
    public static GraphMaterialResponses.MaterialTreeNodeData toMaterialTreeNodeData(
            @NonNull GraphMaterialTreeNodeResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.MaterialTreeNodeData(
                value.id(), value.parentId(), value.title(), value.nodeType(), value.leaf());
    }

    @NonNull
    public static GraphMaterialResponses.NodeData toMaterialNodeData(@NonNull GraphMaterialNode value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.NodeData(
                String.valueOf(value.getId().value()),
                value.getNodeType().name(),
                value.getName(),
                fromJsonObject(value.getPropertiesJson()),
                value.getSource().name());
    }

    @NonNull
    public static GraphMaterialResponses.EdgeData toMaterialEdgeData(@NonNull GraphMaterialEdge value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.EdgeData(
                String.valueOf(value.getId().value()),
                String.valueOf(value.getSourceNodeId().value()),
                String.valueOf(value.getTargetNodeId().value()),
                value.getRelationType(),
                fromJsonObject(value.getQualifiersJson()),
                value.getSource().name());
    }

    @NonNull
    public static GraphMaterialResponses.DetailData toDetailData(
            @NonNull GraphMaterialResult value, @NonNull List<GraphExtractionResponses.TaskData> extractionTasks) {
        return toDetailData(value, extractionTasks, extractionTasks.size(), null);
    }

    @NonNull
    public static GraphMaterialResponses.DetailData toDetailData(
            @NonNull GraphMaterialResult value,
            @NonNull List<GraphExtractionResponses.TaskData> extractionTasks,
            long taskCount) {
        return toDetailData(value, extractionTasks, taskCount, null);
    }

    @NonNull
    public static GraphMaterialResponses.DetailData toDetailData(
            @NonNull GraphMaterialResult value,
            @NonNull List<GraphExtractionResponses.TaskData> extractionTasks,
            long taskCount,
            GraphExtractionCandidatePreviewResult latestTaskCandidate) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(extractionTasks, "extractionTasks");
        return new GraphMaterialResponses.DetailData(
                toNullableSourceData(value.source()),
                value.material() == null ? null : toMaterialData(value.material()),
                toNullableMaterialStatsData(value.materialStats()),
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList(),
                toTaskSummaryData(value, taskCount),
                extractionTasks,
                toNullableCandidateData(latestTaskCandidate));
    }

    private static GraphMaterialResponses.TaskSummaryData toTaskSummaryData(GraphMaterialResult value, long taskCount) {
        var stats = value.materialStats();
        return new GraphMaterialResponses.TaskSummaryData(
                stats == null ? "0" : String.valueOf(stats.getActiveTaskCount()),
                stats == null ? "0" : String.valueOf(stats.getPendingReviewTaskCount()),
                stats == null ? "0" : String.valueOf(stats.getFailedTaskCount()),
                String.valueOf(taskCount),
                toNullableTaskData(value.taskSummary()));
    }

    @NonNull
    public static GraphMaterialResponses.GraphData toGraphData(@NonNull GraphMaterialResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.GraphData(
                value.material() == null ? null : toMaterialData(value.material()),
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList());
    }

    @NonNull
    public static GraphExtractionCommand toCommand(
            @NonNull GraphExtractionRequests.ExtractionCreateRequest request, @NonNull Long requestedBy) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(requestedBy, "requestedBy");
        return new GraphExtractionCommand(toContentRef(request), request.getIdempotencyKey(), requestedBy);
    }

    @NonNull
    public static GraphExtractionBatchCommand toCommand(
            @NonNull GraphExtractionRequests.BatchCreateRequest request, @NonNull Long requestedBy) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(requestedBy, "requestedBy");
        List<ContentRef> materialRefs =
                request.getSelection() == null || request.getSelection().getContentRefs() == null
                        ? List.of()
                        : request.getSelection().getContentRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList();
        return new GraphExtractionBatchCommand(
                materialRefs,
                request.getSelection() == null ? null : request.getSelection().getVolumeCode(),
                request.getIdempotencyKey(),
                requestedBy);
    }

    @NonNull
    public static GraphTaskQuery toQuery(@NonNull GraphExtractionRequests.TaskPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphTaskQuery(
                request.getKeyword(),
                request.getContentType(),
                request.getCategoryCode(),
                request.getVolumeCode(),
                request.getContentRefs() == null
                        ? List.of()
                        : request.getContentRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList(),
                request.getBatchId(),
                request.getExecutionStatus(),
                request.getDisposition(),
                request.getGroupBy());
    }

    @NonNull
    public static GraphTaskDetailQuery toQuery(@NonNull GraphExtractionRequests.TaskGetRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphTaskDetailQuery(Long.valueOf(request.getTaskId()));
    }

    @NonNull
    public static GraphExtractionRetryCommand toRetryCommand(
            @NonNull GraphExtractionRequests.TaskActionRequest request, @NonNull Long requestedBy) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(requestedBy, "requestedBy");
        return new GraphExtractionRetryCommand(
                Long.valueOf(request.getTaskId()),
                Long.parseLong(request.getTaskLockVersion()),
                request.getExpectedExecutionStatus(),
                request.getIdempotencyKey(),
                requestedBy);
    }

    @NonNull
    public static GraphExtractionCancelCommand toCancelCommand(
            @NonNull GraphExtractionRequests.TaskActionRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphExtractionCancelCommand(
                Long.valueOf(request.getTaskId()),
                Long.parseLong(request.getTaskLockVersion()),
                request.getExpectedExecutionStatus(),
                request.getIdempotencyKey());
    }

    @NonNull
    public static GraphExtractionCandidateApplyCommand toCommand(
            @NonNull GraphExtractionRequests.CandidateApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphExtractionCandidateApplyCommand(
                Long.valueOf(request.getTaskId()),
                Long.parseLong(request.getTaskLockVersion()),
                request.getExpectedExecutionStatus(),
                request.getExpectedDisposition(),
                Long.parseLong(request.getMaterialLockVersion()),
                GraphMaterialApplyMode.valueOf(request.getApplyMode()),
                request.getIdempotencyKey());
    }

    @NonNull
    public static GraphExtractionCandidateDiscardCommand toCommand(
            @NonNull GraphExtractionRequests.CandidateDiscardRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphExtractionCandidateDiscardCommand(
                Long.valueOf(request.getTaskId()),
                Long.parseLong(request.getTaskLockVersion()),
                request.getExpectedExecutionStatus(),
                request.getExpectedDisposition(),
                request.getReason(),
                request.getIdempotencyKey());
    }

    @NonNull
    public static GraphExtractionRegenerateCommand toCommand(
            @NonNull GraphExtractionRequests.CandidateRegenerateRequest request, @NonNull Long requestedBy) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(requestedBy, "requestedBy");
        return new GraphExtractionRegenerateCommand(
                Long.valueOf(request.getTaskId()),
                Long.parseLong(request.getTaskLockVersion()),
                request.getExpectedExecutionStatus(),
                request.getExpectedDisposition(),
                request.getIdempotencyKey(),
                requestedBy);
    }

    @NonNull
    public static GraphExtractionResponses.TaskData toTaskData(@NonNull GraphExtractionTaskResult value) {
        Objects.requireNonNull(value, "value");
        return toTaskDataInternal(value);
    }

    private static GraphExtractionResponses.TaskData toNullableTaskData(GraphExtractionTaskResult value) {
        if (value == null) {
            return null;
        }
        return toTaskDataInternal(value);
    }

    private static GraphExtractionResponses.TaskData toTaskDataInternal(GraphExtractionTaskResult value) {
        return new GraphExtractionResponses.TaskData(
                string(value.taskId()),
                toNullableContentRefData(value.contentRef()),
                value.materialTitle(),
                String.valueOf(value.lockVersion()),
                value.executionStatus(),
                value.disposition(),
                String.valueOf(value.attemptNo()),
                String.valueOf(value.progress()),
                value.currentStage(),
                string(value.candidateId()),
                null,
                null,
                value.batchId(),
                null,
                null,
                null,
                instant(value.requestedAt()),
                instant(value.completedAt()),
                instant(value.disposedAt()),
                instant(value.purgeAfter()));
    }

    @NonNull
    public static GraphExtractionResponses.BatchResultData toBatchData(@NonNull GraphExtractionBatchResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphExtractionResponses.BatchResultData(
                value.idempotencyKey(),
                value.tasks().stream().map(GraphInterfaceAssembler::toTaskData).toList());
    }

    @NonNull
    public static GraphExtractionResponses.StageData toStageData(@NonNull GraphExtractionStageResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphExtractionResponses.StageData(
                String.valueOf(value.stageOrder()),
                value.stageName(),
                value.status(),
                String.valueOf(value.progress()),
                value.inputSummaryJson(),
                value.outputSummaryJson(),
                value.failureReason(),
                instant(value.startedAt()),
                instant(value.completedAt()));
    }

    private static GraphExtractionResponses.CandidatePreviewData toNullableCandidateData(
            GraphExtractionCandidatePreviewResult value) {
        return value == null
                ? null
                : new GraphExtractionResponses.CandidatePreviewData(
                        string(value.candidateId()), value.resultFormat(), value.resultSummaryJson());
    }

    @NonNull
    public static GraphExtractionResponses.TaskDetailData toTaskDetailData(
            @NonNull GraphExtractionTaskDetailResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphExtractionResponses.TaskDetailData(
                toTaskData(value.task()),
                null,
                null,
                value.stages().stream()
                        .map(GraphInterfaceAssembler::toStageData)
                        .toList(),
                value.relatedTasks().stream()
                        .map(GraphInterfaceAssembler::toTaskData)
                        .toList(),
                toNullableCandidateData(value.candidate()));
    }

    @NonNull
    public static GraphExtractionResponses.CandidateApplyData toCandidateApplyData(@NonNull GraphMaterialResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphExtractionResponses.CandidateApplyData(
                toNullableTaskData(value.taskSummary()), toDetailData(value, List.of()));
    }

    @NonNull
    public static GraphMaterialDeletionPrecheckCommand toDeletionPrecheckCommand(
            @NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialDeletionPrecheckCommand(toContentRef(request));
    }

    @NonNull
    public static GraphMaterialDeletionChangeQuery toQuery(
            @NonNull GraphDeletionRequests.DeletionChangePageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialDeletionChangeQuery(null);
    }

    @NonNull
    public static GraphMaterialDeletionDecisionCommand toCommand(
            @NonNull GraphDeletionRequests.DeletionDecisionRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialDeletionDecisionCommand(
                GraphMaterialDeletionChangeIdCodec.toDomain(Long.valueOf(request.getChangeId())),
                GraphMaterialDeletionDecision.valueOf(request.getDecision()),
                Long.parseLong(request.getLockVersion()));
    }

    @NonNull
    public static GraphMaterialDeletionTaskQuery toQuery(
            @NonNull GraphDeletionRequests.DeletionTaskPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialDeletionTaskQuery(
                request.getStatus() == null ? null : GraphMaterialDeletionStatus.valueOf(request.getStatus()));
    }

    @NonNull
    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId
            toDeletionTaskId(@NonNull GraphDeletionRequests.DeletionTaskIdRequest request) {
        Objects.requireNonNull(request, "request");
        return GraphMaterialDeletionTaskIdCodec.toDomain(Long.valueOf(request.getTaskId()));
    }

    @NonNull
    public static GraphMaterialDeletionTaskRetryCommand toCommand(
            @NonNull GraphDeletionRequests.DeletionTaskRetryRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialDeletionTaskRetryCommand(
                GraphMaterialDeletionTaskIdCodec.toDomain(Long.valueOf(request.getTaskId())),
                Long.parseLong(request.getLockVersion()));
    }

    @NonNull
    public static GraphDeletionResponses.ChangeData toDeletionChangeData(@NonNull GraphMaterialDeletionChange value) {
        Objects.requireNonNull(value, "value");
        return new GraphDeletionResponses.ChangeData(
                value.getId() == null ? null : String.valueOf(value.getId().value()),
                toNullableContentRefData(value.getMaterialRef()),
                value.getStatus() == null ? null : value.getStatus().name(),
                value.getDecision() == null ? null : value.getDecision().name(),
                String.valueOf(value.getLockVersion()),
                fromJsonObject(value.getMaterialSnapshotJson()),
                instant(value.getRequestedAt()),
                instant(value.getCompletedAt()));
    }

    @NonNull
    public static GraphDeletionResponses.TaskData toDeletionTaskData(@NonNull GraphMaterialDeletionTask value) {
        Objects.requireNonNull(value, "value");
        return new GraphDeletionResponses.TaskData(
                value.getId() == null ? null : String.valueOf(value.getId().value()),
                value.getStatus() == null ? null : value.getStatus().name(),
                String.valueOf(value.getLockVersion()),
                String.valueOf(value.getProgress()),
                value.getDeletionChangeId() == null
                        ? null
                        : String.valueOf(value.getDeletionChangeId().value()),
                value.getResultSummaryJson(),
                value.getFailureReason(),
                null,
                instant(value.getRequestedAt()),
                instant(value.getCompletedAt()));
    }

    @NonNull
    public static GraphMaterialResponses.ChangeImpactData toChangeImpactData(
            @NonNull GraphMaterialChangeImpactResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ChangeImpactData(
                value.nodes().stream()
                        .map(GraphInterfaceAssembler::toMaterialNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphInterfaceAssembler::toMaterialEdgeData)
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.executable());
    }

    @NonNull
    public static GraphMaterialResponses.ImportPreviewData toImportPreviewData(
            @NonNull GraphMaterialImportPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.ImportPreviewData(
                toGraphData(value.importedGraph()),
                value.createdNodeCount(),
                value.updatedNodeCount(),
                value.createdEdgeCount(),
                value.updatedEdgeCount(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.importable());
    }

    @NonNull
    public static GraphMaterialResponses.IssueData toIssueData(@NonNull GraphValidationIssueResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphMaterialResponses.IssueData(
                value.code(), value.severity(), value.objectType(), value.objectId(), value.field(), value.message());
    }

    @NonNull
    public static GraphMaterialNodeCommand toCommand(@NonNull GraphMaterialRequests.MaterialObjectRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeCommand(toMaterialNode(request), Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialEdgeCommand toCommand(@NonNull GraphMaterialRequests.MaterialEdgeRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialEdgeCommand(toMaterialEdge(request), Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeDeleteCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialObjectDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeDeleteCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getNodeId())),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialEdgeDeleteCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialEdgeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialEdgeDeleteCommand(
                toContentRef(request),
                GraphMaterialEdgeIdCodec.toDomain(Long.valueOf(request.getEdgeId())),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeMergeQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialNodeMergePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeMergeQuery(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getRetainedNodeId())),
                request.getMergedNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphMaterialNodeIdCodec::toDomain)
                        .toList());
    }

    @NonNull
    public static GraphMaterialNodeMergeCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialNodeMergeApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeMergeCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getRetainedNodeId())),
                request.getMergedNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphMaterialNodeIdCodec::toDomain)
                        .toList(),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialNodeSplitQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialNodeSplitPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeSplitQuery(
                toContentRef(request), GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())));
    }

    @NonNull
    public static GraphMaterialNodeSplitCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialNodeSplitApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialNodeSplitCommand(
                toContentRef(request),
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())),
                toMaterialNode(toContentRef(request), request.getSplitNode()),
                request.getReassignedEdgeIds() == null
                        ? List.of()
                        : request.getReassignedEdgeIds().stream()
                                .map(Long::valueOf)
                                .map(GraphMaterialEdgeIdCodec::toDomain)
                                .toList(),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphMaterialImportQuery toQuery(
            @NonNull GraphMaterialRequests.MaterialImportPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialImportQuery(toContentRef(request), request.getGraphJson());
    }

    @NonNull
    public static GraphMaterialImportCommand toCommand(
            @NonNull GraphMaterialRequests.MaterialImportApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphMaterialImportCommand(
                toContentRef(request),
                request.getGraphJson(),
                GraphMaterialApplyMode.valueOf(request.getApplyMode()),
                Long.parseLong(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphPublicationPreviewQuery toQuery(
            @NonNull GraphPublicationRequests.PublicationPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublicationPreviewQuery(toContentRef(request));
    }

    @NonNull
    public static GraphBatchPublicationPreviewQuery toQuery(
            @NonNull GraphPublicationRequests.BatchPublicationPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchPublicationPreviewQuery(request.getContentRefs().stream()
                .map(GraphInterfaceAssembler::toContentRef)
                .toList());
    }

    @NonNull
    public static GraphWithdrawalPreviewQuery toWithdrawalPreviewQuery(
            @NonNull GraphMaterialRequests.ContentRefRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphWithdrawalPreviewQuery(toContentRef(request));
    }

    @NonNull
    public static GraphBatchWithdrawalPreviewQuery toQuery(
            @NonNull GraphPublicationRequests.BatchWithdrawalPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchWithdrawalPreviewQuery(request.getContentRefs().stream()
                .map(GraphInterfaceAssembler::toContentRef)
                .toList());
    }

    @NonNull
    public static GraphPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.PublicationConfirmRequest request) {
        return toCommand(request, null);
    }

    @NonNull
    public static GraphPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.PublicationConfirmRequest request, Long publishedBy) {
        Objects.requireNonNull(request, "request");
        return new GraphPublicationCommand(
                toContentRef(request),
                Long.valueOf(request.getMaterialLockVersion()),
                publishedBy,
                request.getPreviewToken(),
                request.getConflictDecisions() == null
                        ? List.of()
                        : request.getConflictDecisions().stream()
                                .map(decision -> new GraphPublicationConflictDecision(
                                        decision.getObjectType(),
                                        Long.valueOf(decision.getMaterialObjectId()),
                                        decision.getAction(),
                                        decision.getMatchedObjectId() == null
                                                ? null
                                                : Long.valueOf(decision.getMatchedObjectId())))
                                .toList());
    }

    @NonNull
    public static GraphBatchPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.BatchPublicationConfirmRequest request) {
        return toCommand(request, null);
    }

    @NonNull
    public static GraphBatchPublicationCommand toCommand(
            @NonNull GraphPublicationRequests.BatchPublicationConfirmRequest request, Long publishedBy) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchPublicationCommand(request.getMaterials().stream()
                .map(material -> toCommand(material, publishedBy))
                .toList());
    }

    @NonNull
    public static GraphWithdrawalCommand toCommand(@NonNull GraphPublicationRequests.WithdrawalRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphWithdrawalCommand(toContentRef(request), Long.valueOf(request.getMaterialLockVersion()));
    }

    @NonNull
    public static GraphBatchWithdrawalCommand toCommand(
            @NonNull GraphPublicationRequests.BatchWithdrawalRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphBatchWithdrawalCommand(
                request.getMaterials().stream()
                        .map(GraphInterfaceAssembler::toCommand)
                        .toList(),
                request.getIdempotencyKey());
    }

    @NonNull
    public static GraphPublicationResponses.PreviewData toPreviewData(@NonNull GraphPublicationPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.PreviewData(
                value.previewToken(),
                toNullableContentRefData(value.materialRef()),
                String.valueOf(value.materialLockVersion()),
                value.nodes().stream()
                        .map(node -> new GraphPublicationResponses.MatchData(
                                String.valueOf(node.materialNode().getId().value()),
                                node.matchedNode() == null ? "CREATE" : "CONFLICT",
                                node.matchedNode() == null
                                        ? null
                                        : String.valueOf(
                                                node.matchedNode().getId().value()),
                                node.matchedNode() == null
                                        ? null
                                        : String.valueOf(node.matchedNode().getLockVersion()),
                                node.issues().stream()
                                        .map(GraphInterfaceAssembler::toIssueData)
                                        .toList()))
                        .toList(),
                value.edges().stream()
                        .map(edge -> new GraphPublicationResponses.MatchData(
                                String.valueOf(edge.materialEdge().getId().value()),
                                edge.matchedEdge() == null ? "CREATE" : "CONFLICT",
                                edge.matchedEdge() == null
                                        ? null
                                        : String.valueOf(
                                                edge.matchedEdge().getId().value()),
                                edge.matchedEdge() == null
                                        ? null
                                        : String.valueOf(edge.matchedEdge().getLockVersion()),
                                edge.issues().stream()
                                        .map(GraphInterfaceAssembler::toIssueData)
                                        .toList()))
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.publishable());
    }

    @NonNull
    public static GraphPublicationResponses.PublicationData toPublicationData(@NonNull GraphPublicationResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.PublicationData(
                toNullableContentRefData(value.materialRef()),
                value.materialStatus().name(),
                value.success(),
                value.failureMessage(),
                value.createdNodeCount(),
                value.reusedNodeCount(),
                value.createdEdgeCount(),
                value.reusedEdgeCount(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchPreviewData toBatchPreviewData(
            @NonNull GraphBatchPublicationPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.BatchPreviewData(value.materials().stream()
                .map(GraphInterfaceAssembler::toPreviewData)
                .toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchData toBatchData(@NonNull GraphBatchPublicationResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.BatchData(value.materials().stream()
                .map(item -> new GraphPublicationResponses.BatchItemData(
                        toNullableContentRefData(item.materialRef()),
                        item.success(),
                        item.success() ? toPublicationData(item) : null,
                        item.success() ? null : "GRAPH_PUBLICATION_FAILED",
                        item.failureMessage()))
                .toList());
    }

    @NonNull
    public static GraphPublicationResponses.WithdrawalPreviewData toWithdrawalPreviewData(
            @NonNull GraphWithdrawalPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.WithdrawalPreviewData(
                toNullableContentRefData(value.materialRef()),
                value.nodeMappingCount(),
                value.edgeMappingCount(),
                value.governedNodes().stream()
                        .map(GraphInterfaceAssembler::toNodeData)
                        .toList(),
                value.governedEdges().stream()
                        .map(GraphInterfaceAssembler::toEdgeData)
                        .toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchWithdrawalPreviewData toBatchWithdrawalPreviewData(
            @NonNull GraphBatchWithdrawalPreviewResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.BatchWithdrawalPreviewData(value.materials().stream()
                .map(item -> new GraphPublicationResponses.BatchWithdrawalPreviewItemData(
                        toNullableContentRefData(item.contentRef()),
                        item.preview() == null ? null : toWithdrawalPreviewData(item.preview()),
                        item.failureCode(),
                        item.failureMessage()))
                .toList());
    }

    @NonNull
    public static GraphPublicationResponses.BatchWithdrawalData toBatchWithdrawalData(
            @NonNull GraphBatchWithdrawalResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublicationResponses.BatchWithdrawalData(
                value.batchId(),
                value.materials().stream()
                        .map(item -> new GraphPublicationResponses.WithdrawalResultData(
                                toNullableContentRefData(item.contentRef()),
                                item.success(),
                                item.result() == null ? null : toMaterialData(item.result()),
                                item.failureCode(),
                                item.failureMessage()))
                        .toList());
    }

    @NonNull
    public static GraphPublishedNodeQuery toQuery(@NonNull GraphPublishedRequests.PublishedNodePageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeQuery(
                request.getKeyword(),
                request.getNodeType() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType.valueOf(
                                request.getNodeType()),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus.valueOf(
                                request.getStatus()),
                request.getSource() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType.valueOf(
                                request.getSource()));
    }

    @NonNull
    public static GraphPublishedEdgeQuery toQuery(@NonNull GraphPublishedRequests.PublishedEdgePageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeQuery(
                request.getKeyword(),
                request.getRelationType(),
                request.getStatus() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus.valueOf(
                                request.getStatus()),
                request.getSource() == null
                        ? null
                        : com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType.valueOf(
                                request.getSource()));
    }

    @NonNull
    public static GraphPublishedAdjacencyQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedAdjacencyPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedAdjacencyQuery(
                request.getSubjectKeyword(),
                request.getSubjectType() == null ? null : GraphNodeType.from(request.getSubjectType()),
                request.getSubjectStatus() == null ? null : GraphPublishedStatus.valueOf(request.getSubjectStatus()),
                request.getSubjectSource() == null ? null : GraphSourceType.valueOf(request.getSubjectSource()),
                request.getRelationType(),
                request.getRelationStatus() == null ? null : GraphPublishedStatus.valueOf(request.getRelationStatus()),
                request.getRelationSource() == null ? null : GraphSourceType.valueOf(request.getRelationSource()),
                request.getObjectKeyword(),
                request.getObjectType() == null ? null : GraphNodeType.from(request.getObjectType()),
                request.getObjectStatus() == null ? null : GraphPublishedStatus.valueOf(request.getObjectStatus()),
                request.getObjectSource() == null ? null : GraphSourceType.valueOf(request.getObjectSource()),
                request.getIncludeIsolated());
    }

    @NonNull
    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId toNodeId(
            @NonNull String value) {
        Objects.requireNonNull(value, "value");
        return GraphPublishedNodeIdCodec.toDomain(Long.valueOf(value));
    }

    @NonNull
    public static com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId toEdgeId(
            @NonNull String value) {
        Objects.requireNonNull(value, "value");
        return GraphPublishedEdgeIdCodec.toDomain(Long.valueOf(value));
    }

    @NonNull
    public static GraphIncidentEdgesQuery toQuery(@NonNull GraphWorkbenchRequests.IncidentEdgesListRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphIncidentEdgesQuery(
                request.getNodeIds().stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodeIdCodec::toDomain)
                        .toList(),
                GraphPublishedEdgeIdCodec.toDomain(
                        request.getAfterEdgeId() == null ? null : Long.valueOf(request.getAfterEdgeId())));
    }

    @NonNull
    public static GraphSearchQuery toQuery(@NonNull GraphWorkbenchRequests.SearchPageRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphSearchQuery(request.getKeyword(), null, request.getRelationType());
    }

    @NonNull
    public static GraphQualityQuery toQuery(@NonNull GraphWorkbenchRequests.QualityGetRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphQualityQuery(request.getIssueType(), null);
    }

    @NonNull
    public static GraphPublishedResponses.NodeData toNodeData(@NonNull GraphPublishedNode value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.NodeData(
                String.valueOf(value.getId().value()),
                value.getNodeType().name(),
                value.getName(),
                value.getSource().name(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()));
    }

    @NonNull
    public static GraphPublishedResponses.EdgeData toEdgeData(@NonNull GraphPublishedEdge value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.EdgeData(
                String.valueOf(value.getId().value()),
                String.valueOf(value.getSourceNodeId().value()),
                String.valueOf(value.getTargetNodeId().value()),
                value.getRelationType(),
                null,
                value.getSource().name(),
                value.getStatus().name(),
                String.valueOf(value.getLockVersion()));
    }

    @NonNull
    public static GraphPublishedResponses.AdjacencyData toAdjacencyData(@NonNull GraphPublishedAdjacencyResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.AdjacencyData(
                toNodeData(value.subject()),
                value.relation() == null ? null : toEdgeData(value.relation()),
                value.object() == null ? null : toNodeData(value.object()),
                value.relation() == null);
    }

    @NonNull
    public static GraphPublishedResponses.PropertyData toPropertyData(@NonNull GraphPublishedNodeProperty value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.PropertyData(
                String.valueOf(value.getId().value()),
                value.getPropertyKey(),
                value.getValue(),
                value.isPreferred(),
                null,
                null);
    }

    @NonNull
    public static GraphPublishedResponses.PropertyData toPropertyData(@NonNull GraphPublishedEdgeProperty value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.PropertyData(
                String.valueOf(value.getId().value()),
                value.getPropertyKey(),
                value.getValue(),
                value.isPreferred(),
                null,
                null);
    }

    @NonNull
    public static GraphPublishedResponses.NodeDetailData toNodeDetailData(
            @NonNull GraphPublishedNodeDetailResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.NodeDetailData(
                toNodeData(value.node()),
                value.properties().stream()
                        .map(GraphInterfaceAssembler::toPropertyData)
                        .toList(),
                value.materials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "NODE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedNodeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.incidentEdges().stream()
                        .map(GraphInterfaceAssembler::toEdgeData)
                        .toList(),
                value.operations().stream()
                        .map(GraphInterfaceAssembler::toOperationData)
                        .toList());
    }

    @NonNull
    public static GraphPublishedResponses.EdgeDetailData toEdgeDetailData(
            @NonNull GraphPublishedEdgeDetailResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.EdgeDetailData(
                toEdgeData(value.edge()),
                toNodeData(value.sourceNode()),
                toNodeData(value.targetNode()),
                value.properties().stream()
                        .map(GraphInterfaceAssembler::toPropertyData)
                        .toList(),
                value.materials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "EDGE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedEdgeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.operations().stream()
                        .map(GraphInterfaceAssembler::toOperationData)
                        .toList());
    }

    @NonNull
    public static GraphPublishedResponses.OperationData toOperationData(@NonNull GraphGovernanceOperationResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.OperationData(
                value.id() == null ? null : String.valueOf(value.id()),
                value.operationType(),
                value.targetType(),
                value.targetId() == null ? null : String.valueOf(value.targetId()),
                value.reason(),
                value.auditLogId() == null ? null : String.valueOf(value.auditLogId()),
                value.operatorId(),
                value.operatorName(),
                value.occurredAt() == null
                        ? null
                        : String.valueOf(value.occurredAt().toEpochMilli()),
                value.beforeSummary(),
                value.afterSummary());
    }

    @NonNull
    public static GraphPublishedResponses.GovernanceImpactData toGovernanceImpactData(
            @NonNull GraphGovernanceImpactResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphPublishedResponses.GovernanceImpactData(
                value.impactToken(),
                value.nodes().stream().map(GraphInterfaceAssembler::toNodeData).toList(),
                value.edges().stream().map(GraphInterfaceAssembler::toEdgeData).toList(),
                value.nodeMaterials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "NODE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedNodeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.edgeMaterials().stream()
                        .map(material -> new GraphPublishedResponses.MappingData(
                                null,
                                "EDGE",
                                null,
                                toNullableContentRefData(material.getMaterialRef()),
                                String.valueOf(material.getPublishedEdgeId().value()),
                                fromJsonObject(material.getSourceSnapshotJson())))
                        .toList(),
                value.issues().stream()
                        .map(GraphInterfaceAssembler::toIssueData)
                        .toList(),
                value.executable());
    }

    @NonNull
    public static GraphPublishedNodeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeSaveRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeCommand(
                toPublishedNode(request.getNode(), request.getLockVersion()),
                toNodeProperties(request),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedEdgeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeCommand(
                toPublishedEdge(request.getEdge(), request.getLockVersion()),
                toEdgeProperties(request),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeDeleteQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeDeletePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeDeleteQuery(toNodeId(request.getNodeId()), request.getCascadeEdges());
    }

    @NonNull
    public static GraphPublishedNodeDeleteCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeDeleteCommand(
                toNodeId(request.getNodeId()),
                request.getCascadeEdges(),
                Long.parseLong(request.getLockVersion()),
                request.getImpactToken(),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedEdgeDeleteQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedEdgeIdRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeDeleteQuery(toEdgeId(request.getEdgeId()));
    }

    @NonNull
    public static GraphPublishedEdgeDeleteCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedEdgeDeleteRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedEdgeDeleteCommand(
                toEdgeId(request.getEdgeId()),
                Long.parseLong(request.getLockVersion()),
                request.getImpactToken(),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeMergeQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeMergePreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeMergeQuery(
                toNodeId(request.getRetainedNodeId()),
                request.getMergedNodeIds().stream()
                        .map(GraphInterfaceAssembler::toNodeId)
                        .toList());
    }

    @NonNull
    public static GraphPublishedNodeMergeCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeMergeRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeMergeCommand(
                toNodeId(request.getRetainedNodeId()),
                request.getMergedNodeIds().stream()
                        .map(GraphInterfaceAssembler::toNodeId)
                        .toList(),
                Long.parseLong(request.getRetainedNodeLockVersion()),
                request.getImpactToken(),
                request.getReason());
    }

    @NonNull
    public static GraphPublishedNodeSplitQuery toQuery(
            @NonNull GraphPublishedRequests.PublishedNodeSplitPreviewRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeSplitQuery(toNodeId(request.getNodeId()));
    }

    @NonNull
    public static GraphPublishedNodeSplitCommand toCommand(
            @NonNull GraphPublishedRequests.PublishedNodeSplitRequest request) {
        Objects.requireNonNull(request, "request");
        return new GraphPublishedNodeSplitCommand(
                toNodeId(request.getNodeId()),
                toPublishedNode(request.getSplitNode(), request.getSourceNodeLockVersion()),
                stringIdsToNodePropertyIds(request.getMovedPropertyIds()),
                stringIdsToNodePropertyIds(request.getCopiedPropertyIds()),
                stringIdsToEdgeIds(request.getReassignedEdgeIds()),
                request.getCopiedEdges() == null
                        ? List.of()
                        : request.getCopiedEdges().stream()
                                .map(edge -> toPublishedEdge(edge, null))
                                .toList(),
                request.getMovedMaterialRefs() == null
                        ? List.of()
                        : request.getMovedMaterialRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList(),
                request.getCopiedMaterialRefs() == null
                        ? List.of()
                        : request.getCopiedMaterialRefs().stream()
                                .map(GraphInterfaceAssembler::toContentRef)
                                .toList(),
                Long.parseLong(request.getSourceNodeLockVersion()),
                request.getImpactToken(),
                request.getReason());
    }

    @NonNull
    public static GraphWorkbenchResponses.SearchData toSearchData(
            @NonNull com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult value) {
        Objects.requireNonNull(value, "value");
        return new GraphWorkbenchResponses.SearchData(
                value.objectType(),
                value.node() == null ? null : toNodeData(value.node()),
                value.edge() == null ? null : toEdgeData(value.edge()));
    }

    private static GraphMaterialNode toMaterialNode(@NonNull GraphMaterialRequests.MaterialObjectRequest request) {
        return toMaterialNode(toContentRef(request), request.getNode());
    }

    private static GraphMaterialNode toMaterialNode(
            ContentRef materialRef, GraphMaterialRequests.MaterialObjectRequestData request) {
        var node = new GraphMaterialNode();
        node.setId(GraphMaterialNodeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        node.setMaterialRef(materialRef);
        node.setNodeType(GraphNodeType.valueOf(request.getNodeType()));
        node.setName(request.getName());
        node.setSource(GraphSourceType.valueOf(request.getSource()));
        node.setPropertiesJson(toJson(request.getProperties()));
        return node;
    }

    private static GraphMaterialEdge toMaterialEdge(@NonNull GraphMaterialRequests.MaterialEdgeRequest request) {
        var edge = new GraphMaterialEdge();
        edge.setId(GraphMaterialEdgeIdCodec.toDomain(
                request.getEdge().getId() == null
                        ? null
                        : Long.valueOf(request.getEdge().getId())));
        edge.setMaterialRef(toContentRef(request));
        edge.setSourceNodeId(
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getEdge().getSourceNodeId())));
        edge.setTargetNodeId(
                GraphMaterialNodeIdCodec.toDomain(Long.valueOf(request.getEdge().getTargetNodeId())));
        edge.setRelationType(request.getEdge().getRelationType());
        edge.setSource(GraphSourceType.valueOf(request.getEdge().getSource()));
        edge.setQualifiersJson(toJson(request.getEdge().getQualifiers()));
        return edge;
    }

    private static GraphPublishedNode toPublishedNode(
            GraphPublishedRequests.PublishedNodeRequestData request, String lockVersion) {
        var node = new GraphPublishedNode();
        node.setId(GraphPublishedNodeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        node.setNodeType(GraphNodeType.valueOf(request.getNodeType()));
        node.setName(request.getName());
        node.setSource(GraphSourceType.valueOf(request.getSource()));
        node.setStatus(GraphPublishedStatus.valueOf(request.getStatus()));
        node.setLockVersion(lockVersion == null ? 0L : Long.parseLong(lockVersion));
        return node;
    }

    private static GraphPublishedEdge toPublishedEdge(
            GraphPublishedRequests.PublishedEdgeRequestData request, String lockVersion) {
        var edge = new GraphPublishedEdge();
        edge.setId(GraphPublishedEdgeIdCodec.toDomain(request.getId() == null ? null : Long.valueOf(request.getId())));
        edge.setSourceNodeId(GraphPublishedNodeIdCodec.toDomain(Long.valueOf(request.getSourceNodeId())));
        edge.setTargetNodeId(GraphPublishedNodeIdCodec.toDomain(Long.valueOf(request.getTargetNodeId())));
        edge.setRelationType(request.getRelationType());
        edge.setSource(GraphSourceType.valueOf(request.getSource()));
        edge.setQualifiersJson(toJson(request.getQualifiers()));
        edge.setStatus(GraphPublishedStatus.valueOf(request.getStatus()));
        edge.setLockVersion(lockVersion == null ? 0L : Long.parseLong(lockVersion));
        return edge;
    }

    private static List<GraphPublishedNodeProperty> toNodeProperties(
            @NonNull GraphPublishedRequests.PublishedNodeSaveRequest request) {
        var nodeId = request.getNode().getId() == null
                ? null
                : GraphPublishedNodeIdCodec.toDomain(
                        Long.valueOf(request.getNode().getId()));
        return request.getProperties().stream()
                .map(property -> new GraphPublishedNodeProperty(
                        GraphPublishedNodePropertyIdCodec.toDomain(
                                property.getId() == null ? null : Long.valueOf(property.getId())),
                        nodeId,
                        property.getPropertyName(),
                        String.valueOf(property.getValue()),
                        property.getPreferred()))
                .toList();
    }

    private static List<GraphPublishedEdgeProperty> toEdgeProperties(
            @NonNull GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        var edgeId = request.getEdge().getId() == null
                ? null
                : GraphPublishedEdgeIdCodec.toDomain(
                        Long.valueOf(request.getEdge().getId()));
        return request.getProperties().stream()
                .map(property -> new GraphPublishedEdgeProperty(
                        GraphPublishedEdgePropertyIdCodec.toDomain(
                                property.getId() == null ? null : Long.valueOf(property.getId())),
                        edgeId,
                        property.getPropertyName(),
                        String.valueOf(property.getValue()),
                        property.getPreferred()))
                .toList();
    }

    private static List<com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId>
            stringIdsToNodePropertyIds(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedNodePropertyIdCodec::toDomain)
                        .toList();
    }

    private static List<com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId>
            stringIdsToEdgeIds(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                        .map(Long::valueOf)
                        .map(GraphPublishedEdgeIdCodec::toDomain)
                        .toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJsonObject(@NonNull String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, Map.class);
        } catch (JsonProcessingException exception) {
            return Map.of();
        }
    }

    private static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ApiException(
                    "GRAPH-API-00001", "knowledge.graph.invalid-json-payload", "图谱 JSON 协议对象无效", exception);
        }
    }

    private static String string(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String instant(Instant value) {
        return value == null ? null : String.valueOf(value.toEpochMilli());
    }
}
