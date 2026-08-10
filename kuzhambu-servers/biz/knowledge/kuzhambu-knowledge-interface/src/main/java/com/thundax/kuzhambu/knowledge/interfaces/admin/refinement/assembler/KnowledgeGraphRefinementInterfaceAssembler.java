package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.knowledge.application.refinement.command.ApplyRefinementTaskCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.OpenRefinementTaskCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityAnnotationQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualitySummaryQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementWorkbenchQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementApplyResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityOptionResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementProgressSummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementWorkbenchItemResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.RefinementRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response.RefinementResponses;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgeGraphRefinementInterfaceAssembler {

    private KnowledgeGraphRefinementInterfaceAssembler() {}

    @NonNull
    public static RefinementWorkbenchQuery toTaskQuery(@NonNull RefinementRequests.TaskPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RefinementWorkbenchQuery(
                request.getTaskType(),
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getSourceCategoryCode(),
                request.getStatus());
    }

    @NonNull
    public static OpenRefinementTaskCommand toOpenTaskCommand(@NonNull RefinementRequests.TaskOpenRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OpenRefinementTaskCommand(request.getGraphVersionId(), request.getOpenedBy());
    }

    @NonNull
    public static RefinementDetailQuery toDetailQuery(@NonNull RefinementRequests.TaskDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RefinementDetailQuery(request.getRefinementTaskId());
    }

    @NonNull
    public static ApplyRefinementTaskCommand toApplyTaskCommand(@NonNull RefinementRequests.TaskApplyRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ApplyRefinementTaskCommand(request.getRefinementTaskId(), request.getAppliedBy());
    }

    @NonNull
    public static QualitySummaryQuery toQualitySummaryQuery(@NonNull RefinementRequests.QualitySummaryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new QualitySummaryQuery(request.getRefinementTaskId());
    }

    @NonNull
    public static UpsertRefinementEntityCommand toEntityCommand(
            @NonNull RefinementRequests.EntityUpsertRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpsertRefinementEntityCommand(
                request.getRefinementTaskId(),
                request.getEntityId(),
                request.getEntityKey(),
                request.getName(),
                request.getEntityType(),
                request.getDescription(),
                request.getSourceRefsJson(),
                request.getSortOrder(),
                request.getOperatorId());
    }

    @NonNull
    public static ConfirmRefinementEntityCommand toConfirmEntityCommand(
            @NonNull RefinementRequests.EntityConfirmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ConfirmRefinementEntityCommand(
                request.getRefinementTaskId(), request.getEntityKey(), request.getOperatorId());
    }

    @NonNull
    public static DeleteRefinementEntityCommand toDeleteEntityCommand(
            @NonNull RefinementRequests.EntityDeleteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeleteRefinementEntityCommand(
                request.getRefinementTaskId(), request.getEntityKey(), request.getOperatorId());
    }

    @NonNull
    public static UpsertRefinementRelationCommand toRelationCommand(
            @NonNull RefinementRequests.RelationUpsertRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpsertRefinementRelationCommand(
                request.getRefinementTaskId(),
                request.getRelationId(),
                request.getRelationKey(),
                request.getSourceEntityKey(),
                request.getTargetEntityKey(),
                request.getSourceName(),
                request.getTargetName(),
                request.getRelationType(),
                request.getEvidence(),
                request.getSourceRefsJson(),
                request.getSortOrder(),
                request.getOperatorId());
    }

    @NonNull
    public static ConfirmRefinementRelationCommand toConfirmRelationCommand(
            @NonNull RefinementRequests.RelationConfirmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ConfirmRefinementRelationCommand(
                request.getRefinementTaskId(), request.getRelationKey(), request.getOperatorId());
    }

    @NonNull
    public static DeleteRefinementRelationCommand toDeleteRelationCommand(
            @NonNull RefinementRequests.RelationDeleteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeleteRefinementRelationCommand(
                request.getRefinementTaskId(), request.getRelationKey(), request.getOperatorId());
    }

    @NonNull
    public static UpsertRefinementLineageNodeCommand toLineageNodeCommand(
            @NonNull RefinementRequests.LineageNodeUpsertRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpsertRefinementLineageNodeCommand(
                request.getRefinementTaskId(),
                request.getNodeId(),
                request.getNodeKey(),
                request.getName(),
                request.getNodeType(),
                request.getGeneration(),
                request.getGender(),
                request.getSourceRefsJson(),
                request.getSortOrder(),
                request.getOperatorId());
    }

    @NonNull
    public static ConfirmRefinementLineageNodeCommand toConfirmLineageNodeCommand(
            @NonNull RefinementRequests.LineageNodeConfirmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ConfirmRefinementLineageNodeCommand(
                request.getRefinementTaskId(), request.getNodeKey(), request.getOperatorId());
    }

    @NonNull
    public static DeleteRefinementLineageNodeCommand toDeleteLineageNodeCommand(
            @NonNull RefinementRequests.LineageNodeDeleteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeleteRefinementLineageNodeCommand(
                request.getRefinementTaskId(), request.getNodeKey(), request.getOperatorId());
    }

    @NonNull
    public static UpsertRefinementLineageRelationCommand toLineageRelationCommand(
            @NonNull RefinementRequests.LineageRelationUpsertRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpsertRefinementLineageRelationCommand(
                request.getRefinementTaskId(),
                request.getRelationId(),
                request.getRelationKey(),
                request.getSourceNodeKey(),
                request.getTargetNodeKey(),
                request.getSourceName(),
                request.getTargetName(),
                request.getRelationType(),
                request.getEvidence(),
                request.getSourceRefsJson(),
                request.getSortOrder(),
                request.getOperatorId());
    }

    @NonNull
    public static ConfirmRefinementLineageRelationCommand toConfirmLineageRelationCommand(
            @NonNull RefinementRequests.LineageRelationConfirmRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ConfirmRefinementLineageRelationCommand(
                request.getRefinementTaskId(), request.getRelationKey(), request.getOperatorId());
    }

    @NonNull
    public static DeleteRefinementLineageRelationCommand toDeleteLineageRelationCommand(
            @NonNull RefinementRequests.LineageRelationDeleteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeleteRefinementLineageRelationCommand(
                request.getRefinementTaskId(), request.getRelationKey(), request.getOperatorId());
    }

    @NonNull
    public static UpsertQualityAnnotationCommand toAnnotationCommand(
            @NonNull RefinementRequests.AnnotationUpsertRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UpsertQualityAnnotationCommand(
                request.getAnnotationId(),
                request.getObjectType(),
                request.getObjectKey(),
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getGraphVersionId(),
                request.getAnnotationStatus(),
                request.getAnnotationLabel(),
                request.getComment(),
                request.getOperatorId());
    }

    @NonNull
    public static DeleteQualityAnnotationCommand toDeleteAnnotationCommand(
            @NonNull RefinementRequests.AnnotationDeleteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DeleteQualityAnnotationCommand(request.getAnnotationId());
    }

    @NonNull
    public static QualityAnnotationQuery toAnnotationQuery(@NonNull RefinementRequests.AnnotationPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new QualityAnnotationQuery(request.getRefinementTaskId(), request.getObjectType());
    }

    @NonNull
    public static RefinementResponses.WorkbenchItemResponse toResponse(@NonNull RefinementWorkbenchItemResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.WorkbenchItemResponse.builder()
                .refinementTaskId(result.getRefinementTaskId())
                .graphVersionId(result.getGraphVersionId())
                .taskType(result.getTaskType())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .sourceCategoryCode(result.getSourceCategoryCode())
                .sourceCategoryName(result.getSourceCategoryName())
                .status(result.getStatus())
                .openedBy(result.getOpenedBy())
                .openedAt(result.getOpenedAt())
                .progressSummary(toResponse(result.getProgressSummary()))
                .build();
    }

    @NonNull
    public static RefinementResponses.DetailResponse toResponse(@NonNull RefinementDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.DetailResponse.builder()
                .refinementTaskId(result.getRefinementTaskId())
                .graphVersionId(result.getGraphVersionId())
                .taskType(result.getTaskType())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .sourceCategoryCode(result.getSourceCategoryCode())
                .sourceCategoryName(result.getSourceCategoryName())
                .status(result.getStatus())
                .progressSummary(toResponse(result.getProgressSummary()))
                .entities(
                        result.getEntities() == null
                                ? java.util.List.of()
                                : result.getEntities().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .relations(
                        result.getRelations() == null
                                ? java.util.List.of()
                                : result.getRelations().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .lineageNodes(
                        result.getLineageNodes() == null
                                ? java.util.List.of()
                                : result.getLineageNodes().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .lineageRelations(
                        result.getLineageRelations() == null
                                ? java.util.List.of()
                                : result.getLineageRelations().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .entityOptions(
                        result.getEntityOptions() == null
                                ? java.util.List.of()
                                : result.getEntityOptions().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    @NonNull
    public static RefinementResponses.ApplyResponse toResponse(@NonNull RefinementApplyResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.ApplyResponse.builder()
                .refinementTaskId(result.getRefinementTaskId())
                .graphVersionId(result.getGraphVersionId())
                .taskType(result.getTaskType())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .sourceCategoryCode(result.getSourceCategoryCode())
                .sourceCategoryName(result.getSourceCategoryName())
                .status(result.getStatus())
                .appliedAt(result.getAppliedAt())
                .graphRefreshRequired(result.getGraphRefreshRequired())
                .regenerateSupported(result.getRegenerateSupported())
                .sourceTaskId(result.getSourceTaskId())
                .selectionScopeJson(result.getSelectionScopeJson())
                .replaceUnconfirmedOnly(result.getReplaceUnconfirmedOnly())
                .triggerSource(result.getTriggerSource())
                .nextAction(result.getNextAction())
                .qualityReportRefreshRequired(result.getQualityReportRefreshRequired())
                .build();
    }

    @NonNull
    public static RefinementResponses.EntityResponse toResponse(@NonNull RefinementEntityResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.EntityResponse.builder()
                .draftId(result.getDraftId())
                .entityId(result.getEntityId())
                .entityKey(result.getEntityKey())
                .originType(result.getOriginType())
                .operationType(result.getOperationType())
                .name(result.getName())
                .entityType(result.getEntityType())
                .description(result.getDescription())
                .confirmationStatus(result.getConfirmationStatus())
                .sourceRefsJson(result.getSourceRefsJson())
                .sortOrder(result.getSortOrder())
                .build();
    }

    @NonNull
    public static RefinementResponses.RelationResponse toResponse(@NonNull RefinementRelationResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.RelationResponse.builder()
                .draftId(result.getDraftId())
                .relationId(result.getRelationId())
                .relationKey(result.getRelationKey())
                .originType(result.getOriginType())
                .operationType(result.getOperationType())
                .sourceEntityKey(result.getSourceEntityKey())
                .targetEntityKey(result.getTargetEntityKey())
                .sourceName(result.getSourceName())
                .targetName(result.getTargetName())
                .relationType(result.getRelationType())
                .evidence(result.getEvidence())
                .confirmationStatus(result.getConfirmationStatus())
                .sourceRefsJson(result.getSourceRefsJson())
                .sortOrder(result.getSortOrder())
                .build();
    }

    @NonNull
    public static RefinementResponses.LineageNodeResponse toResponse(@NonNull RefinementLineageNodeResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.LineageNodeResponse.builder()
                .draftId(result.getDraftId())
                .nodeId(result.getNodeId())
                .nodeKey(result.getNodeKey())
                .originType(result.getOriginType())
                .operationType(result.getOperationType())
                .name(result.getName())
                .nodeType(result.getNodeType())
                .generation(result.getGeneration())
                .gender(result.getGender())
                .confirmationStatus(result.getConfirmationStatus())
                .sourceRefsJson(result.getSourceRefsJson())
                .sortOrder(result.getSortOrder())
                .build();
    }

    @NonNull
    public static RefinementResponses.LineageRelationResponse toResponse(
            @NonNull RefinementLineageRelationResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.LineageRelationResponse.builder()
                .draftId(result.getDraftId())
                .relationId(result.getRelationId())
                .relationKey(result.getRelationKey())
                .originType(result.getOriginType())
                .operationType(result.getOperationType())
                .sourceNodeKey(result.getSourceNodeKey())
                .targetNodeKey(result.getTargetNodeKey())
                .sourceName(result.getSourceName())
                .targetName(result.getTargetName())
                .relationType(result.getRelationType())
                .evidence(result.getEvidence())
                .confirmationStatus(result.getConfirmationStatus())
                .sourceRefsJson(result.getSourceRefsJson())
                .sortOrder(result.getSortOrder())
                .build();
    }

    @NonNull
    public static RefinementResponses.AnnotationResponse toResponse(@NonNull QualityAnnotationResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.AnnotationResponse.builder()
                .annotationId(result.getAnnotationId())
                .objectType(result.getObjectType())
                .objectKey(result.getObjectKey())
                .graphVersionId(result.getGraphVersionId())
                .annotationStatus(result.getAnnotationStatus())
                .annotationLabel(result.getAnnotationLabel())
                .comment(result.getComment())
                .build();
    }

    @NonNull
    public static RefinementResponses.QualitySummaryResponse toResponse(@NonNull QualitySummaryResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return RefinementResponses.QualitySummaryResponse.builder()
                .entityCoverageRate(result.getEntityCoverageRate())
                .relationAccuracyRate(result.getRelationAccuracyRate())
                .completenessRate(result.getCompletenessRate())
                .build();
    }

    private static RefinementResponses.ProgressSummaryResponse toResponse(
            @NonNull RefinementProgressSummaryResult result) {
        return RefinementResponses.ProgressSummaryResponse.builder()
                .entityPendingCount(result.getEntityPendingCount())
                .entityConfirmedCount(result.getEntityConfirmedCount())
                .relationPendingCount(result.getRelationPendingCount())
                .relationConfirmedCount(result.getRelationConfirmedCount())
                .build();
    }

    private static RefinementResponses.EntityOptionResponse toResponse(@NonNull RefinementEntityOptionResult result) {
        return RefinementResponses.EntityOptionResponse.builder()
                .entityKey(result.getEntityKey())
                .name(result.getName())
                .build();
    }
}
