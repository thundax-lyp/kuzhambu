package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityAnnotationPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementWorkbenchPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
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

public final class KnowledgeGraphRefinementInterfaceAssembler {

    private KnowledgeGraphRefinementInterfaceAssembler() {}

    public static RefinementWorkbenchPageQuery toPageQuery(RefinementRequests.TaskPageRequest request) {
        return new RefinementWorkbenchPageQuery(
                request == null ? null : request.getTaskType(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getSourceCategoryCode(),
                request == null ? null : request.getStatus(),
                request == null || request.getPageNo() == null ? 1 : request.getPageNo(),
                request == null || request.getPageSize() == null ? 20 : request.getPageSize());
    }

    public static RefinementDetailQuery toDetailQuery(RefinementRequests.TaskDetailRequest request) {
        return new RefinementDetailQuery(request == null ? null : request.getRefinementTaskId());
    }

    public static UpsertRefinementEntityCommand toEntityCommand(RefinementRequests.EntityUpsertRequest request) {
        return new UpsertRefinementEntityCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getEntityId(),
                request == null ? null : request.getEntityKey(),
                request == null ? null : request.getName(),
                request == null ? null : request.getEntityType(),
                request == null ? null : request.getDescription(),
                request == null ? null : request.getSourceRefsJson(),
                request == null ? null : request.getSortOrder(),
                request == null ? null : request.getOperatorId());
    }

    public static ConfirmRefinementEntityCommand toConfirmEntityCommand(
            RefinementRequests.EntityConfirmRequest request) {
        return new ConfirmRefinementEntityCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getEntityKey(),
                request == null ? null : request.getOperatorId());
    }

    public static DeleteRefinementEntityCommand toDeleteEntityCommand(RefinementRequests.EntityDeleteRequest request) {
        return new DeleteRefinementEntityCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getEntityKey(),
                request == null ? null : request.getOperatorId());
    }

    public static UpsertRefinementRelationCommand toRelationCommand(RefinementRequests.RelationUpsertRequest request) {
        return new UpsertRefinementRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getSourceEntityKey(),
                request == null ? null : request.getTargetEntityKey(),
                request == null ? null : request.getSourceName(),
                request == null ? null : request.getTargetName(),
                request == null ? null : request.getRelationType(),
                request == null ? null : request.getEvidence(),
                request == null ? null : request.getSourceRefsJson(),
                request == null ? null : request.getSortOrder(),
                request == null ? null : request.getOperatorId());
    }

    public static ConfirmRefinementRelationCommand toConfirmRelationCommand(
            RefinementRequests.RelationConfirmRequest request) {
        return new ConfirmRefinementRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getOperatorId());
    }

    public static DeleteRefinementRelationCommand toDeleteRelationCommand(
            RefinementRequests.RelationDeleteRequest request) {
        return new DeleteRefinementRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getOperatorId());
    }

    public static UpsertRefinementLineageNodeCommand toLineageNodeCommand(
            RefinementRequests.LineageNodeUpsertRequest request) {
        return new UpsertRefinementLineageNodeCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getNodeId(),
                request == null ? null : request.getNodeKey(),
                request == null ? null : request.getName(),
                request == null ? null : request.getNodeType(),
                request == null ? null : request.getGeneration(),
                request == null ? null : request.getGender(),
                request == null ? null : request.getSourceRefsJson(),
                request == null ? null : request.getSortOrder(),
                request == null ? null : request.getOperatorId());
    }

    public static ConfirmRefinementLineageNodeCommand toConfirmLineageNodeCommand(
            RefinementRequests.LineageNodeConfirmRequest request) {
        return new ConfirmRefinementLineageNodeCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getNodeKey(),
                request == null ? null : request.getOperatorId());
    }

    public static DeleteRefinementLineageNodeCommand toDeleteLineageNodeCommand(
            RefinementRequests.LineageNodeDeleteRequest request) {
        return new DeleteRefinementLineageNodeCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getNodeKey(),
                request == null ? null : request.getOperatorId());
    }

    public static UpsertRefinementLineageRelationCommand toLineageRelationCommand(
            RefinementRequests.LineageRelationUpsertRequest request) {
        return new UpsertRefinementLineageRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getSourceNodeKey(),
                request == null ? null : request.getTargetNodeKey(),
                request == null ? null : request.getSourceName(),
                request == null ? null : request.getTargetName(),
                request == null ? null : request.getRelationType(),
                request == null ? null : request.getEvidence(),
                request == null ? null : request.getSourceRefsJson(),
                request == null ? null : request.getSortOrder(),
                request == null ? null : request.getOperatorId());
    }

    public static ConfirmRefinementLineageRelationCommand toConfirmLineageRelationCommand(
            RefinementRequests.LineageRelationConfirmRequest request) {
        return new ConfirmRefinementLineageRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getOperatorId());
    }

    public static DeleteRefinementLineageRelationCommand toDeleteLineageRelationCommand(
            RefinementRequests.LineageRelationDeleteRequest request) {
        return new DeleteRefinementLineageRelationCommand(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getRelationKey(),
                request == null ? null : request.getOperatorId());
    }

    public static UpsertQualityAnnotationCommand toAnnotationCommand(
            RefinementRequests.AnnotationUpsertRequest request) {
        return new UpsertQualityAnnotationCommand(
                request == null ? null : request.getAnnotationId(),
                request == null ? null : request.getObjectType(),
                request == null ? null : request.getObjectKey(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getGraphVersionId(),
                request == null ? null : request.getAnnotationStatus(),
                request == null ? null : request.getAnnotationLabel(),
                request == null ? null : request.getComment(),
                request == null ? null : request.getOperatorId());
    }

    public static DeleteQualityAnnotationCommand toDeleteAnnotationCommand(
            RefinementRequests.AnnotationDeleteRequest request) {
        return new DeleteQualityAnnotationCommand(request == null ? null : request.getAnnotationId());
    }

    public static QualityAnnotationPageQuery toAnnotationPageQuery(RefinementRequests.AnnotationPageRequest request) {
        return new QualityAnnotationPageQuery(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getObjectType(),
                request == null || request.getPageNo() == null ? 1 : request.getPageNo(),
                request == null || request.getPageSize() == null ? 20 : request.getPageSize());
    }

    public static RefinementResponses.WorkbenchItemResponse toResponse(RefinementWorkbenchItemResult result) {
        return RefinementResponses.WorkbenchItemResponse.builder()
                .refinementTaskId(result == null ? null : result.getRefinementTaskId())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .taskType(result == null ? null : result.getTaskType())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .sourceCategoryCode(result == null ? null : result.getSourceCategoryCode())
                .sourceCategoryName(result == null ? null : result.getSourceCategoryName())
                .status(result == null ? null : result.getStatus())
                .openedBy(result == null ? null : result.getOpenedBy())
                .openedAt(result == null ? null : result.getOpenedAt())
                .progressSummary(toResponse(result == null ? null : result.getProgressSummary()))
                .build();
    }

    public static RefinementResponses.DetailResponse toResponse(RefinementDetailResult result) {
        return RefinementResponses.DetailResponse.builder()
                .refinementTaskId(result == null ? null : result.getRefinementTaskId())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .taskType(result == null ? null : result.getTaskType())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .sourceCategoryCode(result == null ? null : result.getSourceCategoryCode())
                .sourceCategoryName(result == null ? null : result.getSourceCategoryName())
                .status(result == null ? null : result.getStatus())
                .progressSummary(toResponse(result == null ? null : result.getProgressSummary()))
                .entities(
                        result == null || result.getEntities() == null
                                ? java.util.List.of()
                                : result.getEntities().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .relations(
                        result == null || result.getRelations() == null
                                ? java.util.List.of()
                                : result.getRelations().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .lineageNodes(
                        result == null || result.getLineageNodes() == null
                                ? java.util.List.of()
                                : result.getLineageNodes().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .lineageRelations(
                        result == null || result.getLineageRelations() == null
                                ? java.util.List.of()
                                : result.getLineageRelations().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .entityOptions(
                        result == null || result.getEntityOptions() == null
                                ? java.util.List.of()
                                : result.getEntityOptions().stream()
                                        .map(KnowledgeGraphRefinementInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    public static RefinementResponses.EntityResponse toResponse(RefinementEntityResult result) {
        return RefinementResponses.EntityResponse.builder()
                .draftId(result == null ? null : result.getDraftId())
                .entityId(result == null ? null : result.getEntityId())
                .entityKey(result == null ? null : result.getEntityKey())
                .originType(result == null ? null : result.getOriginType())
                .operationType(result == null ? null : result.getOperationType())
                .name(result == null ? null : result.getName())
                .entityType(result == null ? null : result.getEntityType())
                .description(result == null ? null : result.getDescription())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .sortOrder(result == null ? null : result.getSortOrder())
                .build();
    }

    public static RefinementResponses.RelationResponse toResponse(RefinementRelationResult result) {
        return RefinementResponses.RelationResponse.builder()
                .draftId(result == null ? null : result.getDraftId())
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .originType(result == null ? null : result.getOriginType())
                .operationType(result == null ? null : result.getOperationType())
                .sourceEntityKey(result == null ? null : result.getSourceEntityKey())
                .targetEntityKey(result == null ? null : result.getTargetEntityKey())
                .sourceName(result == null ? null : result.getSourceName())
                .targetName(result == null ? null : result.getTargetName())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .sortOrder(result == null ? null : result.getSortOrder())
                .build();
    }

    public static RefinementResponses.LineageNodeResponse toResponse(RefinementLineageNodeResult result) {
        return RefinementResponses.LineageNodeResponse.builder()
                .draftId(result == null ? null : result.getDraftId())
                .nodeId(result == null ? null : result.getNodeId())
                .nodeKey(result == null ? null : result.getNodeKey())
                .originType(result == null ? null : result.getOriginType())
                .operationType(result == null ? null : result.getOperationType())
                .name(result == null ? null : result.getName())
                .nodeType(result == null ? null : result.getNodeType())
                .generation(result == null ? null : result.getGeneration())
                .gender(result == null ? null : result.getGender())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .sortOrder(result == null ? null : result.getSortOrder())
                .build();
    }

    public static RefinementResponses.LineageRelationResponse toResponse(RefinementLineageRelationResult result) {
        return RefinementResponses.LineageRelationResponse.builder()
                .draftId(result == null ? null : result.getDraftId())
                .relationId(result == null ? null : result.getRelationId())
                .relationKey(result == null ? null : result.getRelationKey())
                .originType(result == null ? null : result.getOriginType())
                .operationType(result == null ? null : result.getOperationType())
                .sourceNodeKey(result == null ? null : result.getSourceNodeKey())
                .targetNodeKey(result == null ? null : result.getTargetNodeKey())
                .sourceName(result == null ? null : result.getSourceName())
                .targetName(result == null ? null : result.getTargetName())
                .relationType(result == null ? null : result.getRelationType())
                .evidence(result == null ? null : result.getEvidence())
                .confirmationStatus(result == null ? null : result.getConfirmationStatus())
                .sourceRefsJson(result == null ? null : result.getSourceRefsJson())
                .sortOrder(result == null ? null : result.getSortOrder())
                .build();
    }

    public static RefinementResponses.AnnotationResponse toResponse(QualityAnnotationResult result) {
        return RefinementResponses.AnnotationResponse.builder()
                .annotationId(result == null ? null : result.getAnnotationId())
                .objectType(result == null ? null : result.getObjectType())
                .objectKey(result == null ? null : result.getObjectKey())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .annotationStatus(result == null ? null : result.getAnnotationStatus())
                .annotationLabel(result == null ? null : result.getAnnotationLabel())
                .comment(result == null ? null : result.getComment())
                .build();
    }

    public static RefinementResponses.QualitySummaryResponse toResponse(QualitySummaryResult result) {
        return RefinementResponses.QualitySummaryResponse.builder()
                .entityCoverageRate(result == null ? null : result.getEntityCoverageRate())
                .relationAccuracyRate(result == null ? null : result.getRelationAccuracyRate())
                .completenessRate(result == null ? null : result.getCompletenessRate())
                .build();
    }

    private static RefinementResponses.ProgressSummaryResponse toResponse(RefinementProgressSummaryResult result) {
        return RefinementResponses.ProgressSummaryResponse.builder()
                .entityPendingCount(result == null ? null : result.getEntityPendingCount())
                .entityConfirmedCount(result == null ? null : result.getEntityConfirmedCount())
                .relationPendingCount(result == null ? null : result.getRelationPendingCount())
                .relationConfirmedCount(result == null ? null : result.getRelationConfirmedCount())
                .build();
    }

    private static RefinementResponses.EntityOptionResponse toResponse(RefinementEntityOptionResult result) {
        return RefinementResponses.EntityOptionResponse.builder()
                .entityKey(result == null ? null : result.getEntityKey())
                .name(result == null ? null : result.getName())
                .build();
    }
}
