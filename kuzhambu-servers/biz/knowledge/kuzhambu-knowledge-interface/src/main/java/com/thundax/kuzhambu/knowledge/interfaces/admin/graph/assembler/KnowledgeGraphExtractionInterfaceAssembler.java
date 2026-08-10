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

    @NonNull
    public static RequestRelationExtractionCommand toRelationCommand(
            @NonNull GraphExtractionRequests.CreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RequestRelationExtractionCommand(
                request.getScopeType(),
                request.getScopeJson(),
                request.getTriggerSource(),
                request.getSelectionScopeJson(),
                request.getReplaceUnconfirmedOnly(),
                null,
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getRequestedBy(),
                request.getServiceId(),
                request.getServiceRole(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                Boolean.TRUE.equals(request.getForceJson()),
                request.getLocale());
    }

    @NonNull
    public static RequestGraphExtractionCommand toGraphCommand(@NonNull GraphExtractionRequests.CreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RequestGraphExtractionCommand(
                request.getScopeType(),
                request.getScopeJson(),
                request.getTriggerSource(),
                request.getSelectionScopeJson(),
                request.getReplaceUnconfirmedOnly(),
                null,
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getRequestedBy(),
                request.getServiceId(),
                request.getServiceRole(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                Boolean.TRUE.equals(request.getForceJson()),
                request.getLocale());
    }

    @NonNull
    public static RequestLineageExtractionCommand toLineageCommand(
            @NonNull GraphExtractionRequests.CreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RequestLineageExtractionCommand(
                request.getScopeType(),
                request.getScopeJson(),
                request.getTriggerSource(),
                request.getSelectionScopeJson(),
                request.getReplaceUnconfirmedOnly(),
                null,
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getRequestedBy(),
                request.getServiceId(),
                request.getServiceRole(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                Boolean.TRUE.equals(request.getForceJson()),
                request.getLocale());
    }

    @NonNull
    public static GraphExtractionTaskId toTaskId(@NonNull GraphExtractionRequests.TaskIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return GraphExtractionTaskIdCodec.toDomain(request.getTaskId());
    }

    @NonNull
    public static GraphExtractionTaskId toSourceTaskId(@NonNull GraphExtractionRequests.RegenerateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return GraphExtractionTaskIdCodec.toDomain(request.getSourceTaskId());
    }

    @NonNull
    public static RegenerateGraphExtractionCommand toRegenerateCommand(
            @NonNull GraphExtractionRequests.RegenerateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RegenerateGraphExtractionCommand(
                request.getTaskType(),
                toSourceTaskId(request),
                request.getTriggerSource(),
                request.getSelectionScopeJson(),
                request.getReplaceUnconfirmedOnly(),
                request.getRequestedBy());
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

    @NonNull
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

    @NonNull
    public static GraphExtractionResponses.TaskResponse toResponse(@NonNull GraphExtractionTaskResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.TaskResponse.builder()
                .taskId(result.getTaskId())
                .batchJobId(result.getBatchJobId())
                .taskType(result.getTaskType())
                .scopeType(result.getScopeType())
                .scopeJson(result.getScopeJson())
                .triggerSource(result.getTriggerSource())
                .selectionScopeJson(result.getSelectionScopeJson())
                .replaceUnconfirmedOnly(result.getReplaceUnconfirmedOnly())
                .parentTaskId(result.getParentTaskId())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .aiCallId(result.getAiCallId())
                .aiCandidateId(result.getAiCandidateId())
                .status(result.getStatus())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .requestedBy(result.getRequestedBy())
                .requestedAt(result.getRequestedAt())
                .completedAt(result.getCompletedAt())
                .appliedAt(result.getAppliedAt())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.BatchCancelResponse toResponse(
            @NonNull GraphExtractionBatchCancelResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.BatchCancelResponse.builder()
                .batchJobId(result.getBatchJobId())
                .status(result.getStatus())
                .cancelledCount(result.getCancelledCount())
                .completedCount(result.getCompletedCount())
                .failedCount(result.getFailedCount())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.VersionResponse toResponse(@NonNull GraphVersionResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.VersionResponse.builder()
                .versionId(result.getVersionId())
                .taskId(result.getTaskId())
                .candidateId(result.getCandidateId())
                .taskType(result.getTaskType())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .versionNo(result.getVersionNo())
                .status(result.getStatus())
                .appliedAt(result.getAppliedAt())
                .refinementApplied(result.getRefinementApplied())
                .lastRefinementTaskId(result.getLastRefinementTaskId())
                .lastRefinementAppliedAt(result.getLastRefinementAppliedAt())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.EntityResponse toResponse(@NonNull KnowledgeEntityResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.EntityResponse.builder()
                .entityId(result.getEntityId())
                .entityKey(result.getEntityKey())
                .name(result.getName())
                .entityType(result.getEntityType())
                .description(result.getDescription())
                .confirmationStatus(result.getConfirmationStatus())
                .latestVersionId(result.getLatestVersionId())
                .sourceRefsJson(result.getSourceRefsJson())
                .firstExtractedAt(result.getFirstExtractedAt())
                .lastExtractedAt(result.getLastExtractedAt())
                .confirmedAt(result.getConfirmedAt())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.RelationResponse toResponse(@NonNull KnowledgeRelationResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.RelationResponse.builder()
                .relationId(result.getRelationId())
                .relationKey(result.getRelationKey())
                .sourceName(result.getSourceName())
                .sourceType(result.getSourceType())
                .targetName(result.getTargetName())
                .targetType(result.getTargetType())
                .relationType(result.getRelationType())
                .evidence(result.getEvidence())
                .confirmationStatus(result.getConfirmationStatus())
                .latestVersionId(result.getLatestVersionId())
                .sourceRefsJson(result.getSourceRefsJson())
                .firstExtractedAt(result.getFirstExtractedAt())
                .lastExtractedAt(result.getLastExtractedAt())
                .confirmedAt(result.getConfirmedAt())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.LineageNodeResponse toResponse(@NonNull KnowledgeLineageNodeResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.LineageNodeResponse.builder()
                .nodeId(result.getNodeId())
                .nodeKey(result.getNodeKey())
                .name(result.getName())
                .nodeType(result.getNodeType())
                .generation(result.getGeneration())
                .gender(result.getGender())
                .confirmationStatus(result.getConfirmationStatus())
                .latestVersionId(result.getLatestVersionId())
                .sourceRefsJson(result.getSourceRefsJson())
                .firstExtractedAt(result.getFirstExtractedAt())
                .lastExtractedAt(result.getLastExtractedAt())
                .confirmedAt(result.getConfirmedAt())
                .build();
    }

    @NonNull
    public static GraphExtractionResponses.LineageRelationResponse toResponse(
            @NonNull KnowledgeLineageRelationResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return GraphExtractionResponses.LineageRelationResponse.builder()
                .relationId(result.getRelationId())
                .relationKey(result.getRelationKey())
                .sourceName(result.getSourceName())
                .targetName(result.getTargetName())
                .relationType(result.getRelationType())
                .evidence(result.getEvidence())
                .confirmationStatus(result.getConfirmationStatus())
                .latestVersionId(result.getLatestVersionId())
                .sourceRefsJson(result.getSourceRefsJson())
                .firstExtractedAt(result.getFirstExtractedAt())
                .lastExtractedAt(result.getLastExtractedAt())
                .confirmedAt(result.getConfirmedAt())
                .build();
    }
}
