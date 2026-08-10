package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler;

import com.thundax.kuzhambu.knowledge.application.graph.command.ApplyGraphExtractionTaskCandidateCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.CancelGraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphExtractionTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphVersionQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeEntityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeLineageNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeLineageRelationQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeRelationQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgeGraphExtractionInterfaceAssembler {

    private KnowledgeGraphExtractionInterfaceAssembler() {}

    public static RequestRelationExtractionCommand toRelationCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestRelationExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getTriggerSource(),
                request == null ? null : request.getSelectionScopeJson(),
                request == null ? null : request.getReplaceUnconfirmedOnly(),
                null,
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
    }

    public static RequestGraphExtractionCommand toGraphCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestGraphExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getTriggerSource(),
                request == null ? null : request.getSelectionScopeJson(),
                request == null ? null : request.getReplaceUnconfirmedOnly(),
                null,
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
    }

    public static RequestLineageExtractionCommand toLineageCommand(GraphExtractionRequests.CreateRequest request) {
        return new RequestLineageExtractionCommand(
                request == null ? null : request.getScopeType(),
                request == null ? null : request.getScopeJson(),
                request == null ? null : request.getTriggerSource(),
                request == null ? null : request.getSelectionScopeJson(),
                request == null ? null : request.getReplaceUnconfirmedOnly(),
                null,
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getRequestedBy(),
                request == null ? null : request.getServiceId(),
                request == null ? null : request.getServiceRole(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getTraceId(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getPromptVariablesJson(),
                request == null ? null : request.getPromptHash(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getOutputSchemaJson(),
                request != null && Boolean.TRUE.equals(request.getForceJson()),
                request == null ? null : request.getLocale());
    }

    public static GraphExtractionTaskId toTaskId(GraphExtractionRequests.TaskIdRequest request) {
        return request == null ? null : GraphExtractionTaskIdCodec.toDomain(request.getTaskId());
    }

    public static GraphExtractionTaskId toSourceTaskId(GraphExtractionRequests.RegenerateRequest request) {
        return request == null ? null : GraphExtractionTaskIdCodec.toDomain(request.getSourceTaskId());
    }

    public static RegenerateGraphExtractionCommand toRegenerateCommand(
            GraphExtractionRequests.RegenerateRequest request) {
        return new RegenerateGraphExtractionCommand(
                request == null ? null : request.getTaskType(),
                toSourceTaskId(request),
                request == null ? null : request.getTriggerSource(),
                request == null ? null : request.getSelectionScopeJson(),
                request == null ? null : request.getReplaceUnconfirmedOnly(),
                request == null ? null : request.getRequestedBy());
    }

    @NonNull
    public static GraphExtractionTaskQuery toTaskPageQuery(@NonNull GraphExtractionRequests.PageTaskRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GraphExtractionTaskQuery(
                request.getTaskType(),
                request.getBatchJobId(),
                request.getTriggerSource(),
                request.getStatus(),
                request.getSourceContentType(),
                request.getSourceContentId());
    }

    @NonNull
    public static GraphVersionQuery toVersionPageQuery(@NonNull GraphExtractionRequests.VersionPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GraphVersionQuery(
                request.getTaskType(),
                request.getStatus(),
                request.getSourceContentType(),
                request.getSourceContentId());
    }

    @NonNull
    public static GraphVersionId toVersionId(@NonNull GraphExtractionRequests.VersionIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return GraphVersionIdCodec.toDomain(request.getVersionId());
    }

    @NonNull
    public static KnowledgeEntityQuery toEntityQuery(@NonNull GraphExtractionRequests.EntityPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeEntityQuery(
                request.getVersionId(), request.getKeyword(), request.getEntityType(), request.getConfirmationStatus());
    }

    @NonNull
    public static KnowledgeEntityId toEntityId(@NonNull GraphExtractionRequests.EntityIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return KnowledgeEntityIdCodec.toDomain(request.getEntityId());
    }

    @NonNull
    public static KnowledgeRelationQuery toRelationQuery(@NonNull GraphExtractionRequests.RelationPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeRelationQuery(
                request.getVersionId(),
                request.getKeyword(),
                request.getRelationType(),
                request.getConfirmationStatus(),
                null);
    }

    @NonNull
    public static KnowledgeRelationQuery toRelationQuery(@NonNull GraphExtractionRequests.RelationIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeRelationQuery(null, null, null, null, request.getRelationId());
    }

    @NonNull
    public static KnowledgeLineageNodeQuery toLineageNodeQuery(
            @NonNull GraphExtractionRequests.LineageNodePageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeLineageNodeQuery(
                request.getVersionId(),
                request.getKeyword(),
                request.getNodeType(),
                request.getConfirmationStatus(),
                null);
    }

    @NonNull
    public static KnowledgeLineageNodeQuery toLineageNodeQuery(
            @NonNull GraphExtractionRequests.LineageNodeIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeLineageNodeQuery(null, null, null, null, request.getNodeId());
    }

    @NonNull
    public static KnowledgeLineageRelationQuery toLineageRelationQuery(
            @NonNull GraphExtractionRequests.LineageRelationPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeLineageRelationQuery(
                request.getVersionId(),
                request.getKeyword(),
                request.getRelationType(),
                request.getConfirmationStatus(),
                null);
    }

    @NonNull
    public static KnowledgeLineageRelationQuery toLineageRelationQuery(
            @NonNull GraphExtractionRequests.LineageRelationIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgeLineageRelationQuery(null, null, null, null, request.getRelationId());
    }

    public static CancelGraphExtractionBatchCommand toCancelBatchCommand(
            @NonNull GraphExtractionRequests.BatchCancelRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CancelGraphExtractionBatchCommand(request.getBatchJobId(), request.getRequestedBy());
    }

    @NonNull
    public static ApplyGraphExtractionTaskCandidateCommand toApplyTaskCandidateCommand(
            @NonNull GraphExtractionRequests.TaskIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ApplyGraphExtractionTaskCandidateCommand(toTaskId(request), null);
    }

    public static GraphExtractionResponses.TaskResponse toResponse(GraphExtractionTaskResult result) {
        return GraphExtractionResponses.TaskResponse.builder()
                .taskId(result == null ? null : result.getTaskId())
                .batchJobId(result == null ? null : result.getBatchJobId())
                .taskType(result == null ? null : result.getTaskType())
                .scopeType(result == null ? null : result.getScopeType())
                .scopeJson(result == null ? null : result.getScopeJson())
                .triggerSource(result == null ? null : result.getTriggerSource())
                .selectionScopeJson(result == null ? null : result.getSelectionScopeJson())
                .replaceUnconfirmedOnly(result == null ? null : result.getReplaceUnconfirmedOnly())
                .parentTaskId(result == null ? null : result.getParentTaskId())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .aiCallId(result == null ? null : result.getAiCallId())
                .aiCandidateId(result == null ? null : result.getAiCandidateId())
                .status(result == null ? null : result.getStatus())
                .errorType(result == null ? null : result.getErrorType())
                .errorMessage(result == null ? null : result.getErrorMessage())
                .requestedBy(result == null ? null : result.getRequestedBy())
                .requestedAt(result == null ? null : result.getRequestedAt())
                .completedAt(result == null ? null : result.getCompletedAt())
                .appliedAt(result == null ? null : result.getAppliedAt())
                .build();
    }

    public static GraphExtractionResponses.BatchCancelResponse toResponse(GraphExtractionBatchCancelResult result) {
        return GraphExtractionResponses.BatchCancelResponse.builder()
                .batchJobId(result == null ? null : result.getBatchJobId())
                .status(result == null ? null : result.getStatus())
                .cancelledCount(result == null ? null : result.getCancelledCount())
                .completedCount(result == null ? null : result.getCompletedCount())
                .failedCount(result == null ? null : result.getFailedCount())
                .build();
    }

    public static GraphExtractionResponses.VersionResponse toResponse(GraphVersionResult result) {
        return GraphExtractionResponses.VersionResponse.builder()
                .versionId(result == null ? null : result.getVersionId())
                .taskId(result == null ? null : result.getTaskId())
                .candidateId(result == null ? null : result.getCandidateId())
                .taskType(result == null ? null : result.getTaskType())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .versionNo(result == null ? null : result.getVersionNo())
                .status(result == null ? null : result.getStatus())
                .appliedAt(result == null ? null : result.getAppliedAt())
                .refinementApplied(result == null ? null : result.getRefinementApplied())
                .lastRefinementTaskId(result == null ? null : result.getLastRefinementTaskId())
                .lastRefinementAppliedAt(result == null ? null : result.getLastRefinementAppliedAt())
                .build();
    }

    public static GraphExtractionResponses.EntityResponse toResponse(KnowledgeEntityResult result) {
        return GraphExtractionResponses.EntityResponse.builder()
                .entityId(result == null ? null : result.getEntityId())
                .entityKey(result == null ? null : result.getEntityKey())
                .name(result == null ? null : result.getName())
                .entityType(result == null ? null : result.getEntityType())
                .description(result == null ? null : result.getDescription())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.RelationResponse toResponse(KnowledgeRelationResult result) {
        return GraphExtractionResponses.RelationResponse.builder()
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .sourceName(result == null ? null : result.getSourceName())
                .sourceType(result == null ? null : result.getSourceType())
                .targetName(result == null ? null : result.getTargetName())
                .targetType(result == null ? null : result.getTargetType())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.LineageNodeResponse toResponse(KnowledgeLineageNodeResult result) {
        return GraphExtractionResponses.LineageNodeResponse.builder()
                .nodeId(result == null ? null : result.getNodeId())
                .nodeKey(result == null ? null : result.getNodeKey())
                .name(result == null ? null : result.getName())
                .nodeType(result == null ? null : result.getNodeType())
                .generation(result == null ? null : result.getGeneration())
                .gender(result == null ? null : result.getGender())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }

    public static GraphExtractionResponses.LineageRelationResponse toResponse(KnowledgeLineageRelationResult result) {
        return GraphExtractionResponses.LineageRelationResponse.builder()
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .sourceName(result == null ? null : result.getSourceName())
                .targetName(result == null ? null : result.getTargetName())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .latestVersionId(result == null ? null : result.getLatestVersionId())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .firstExtractedAt(result == null ? null : result.getFirstExtractedAt())
                .lastExtractedAt(result == null ? null : result.getLastExtractedAt())
                .confirmedAt(result == null ? null : result.getConfirmedAt())
                .build();
    }
}
