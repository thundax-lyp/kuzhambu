package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.assembler;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.KnowledgePortalLineageController;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.response.KnowledgePortalLineageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgePortalLineageInterfaceAssembler {

    private KnowledgePortalLineageInterfaceAssembler() {}

    @NonNull
    public static LineageCanvasQuery toQuery(@NonNull KnowledgePortalLineageController.Query request) {
        Objects.requireNonNull(request, "request must not be null");
        return new LineageCanvasQuery(
                request.getVersionId(),
                request.getFocusNodeId(),
                request.getFocusRelationId(),
                request.getKeyword(),
                request.getNodeType(),
                request.getRelationType(),
                request.getConfirmationStatus(),
                request.getDepth());
    }

    @NonNull
    public static KnowledgePortalLineageResponse toResponse(@NonNull LineageCanvasResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgePortalLineageResponse.builder()
                .version(toVersionResponse(result.getVersion()))
                .summary(toSummaryResponse(result.getSummary()))
                .nodes(result.getNodes().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toNodeResponse)
                        .toList())
                .relations(result.getRelations().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toRelationResponse)
                        .toList())
                .selectedNode(toNodeResponse(result.getSelectedNode()))
                .selectedRelation(toRelationResponse(result.getSelectedRelation()))
                .availableFilters(toAvailableFiltersResponse(result.getAvailableFilters()))
                .empty(toEmptyResponse(result.getEmpty()))
                .build();
    }

    private static KnowledgePortalLineageResponse.VersionResponse toVersionResponse(
            LineageCanvasResult.VersionView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.VersionResponse.builder()
                .versionId(view.getVersionId())
                .versionNo(view.getVersionNo())
                .taskType(view.getTaskType())
                .status(view.getStatus())
                .sourceContentType(view.getSourceContentType())
                .sourceContentId(view.getSourceContentId())
                .sourceCategoryCode(view.getSourceCategoryCode())
                .sourceCategoryName(view.getSourceCategoryName())
                .appliedAt(view.getAppliedAt())
                .build();
    }

    private static KnowledgePortalLineageResponse.VersionResponse toVersionResponse(
            LineageCanvasResult.VersionOptionView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.VersionResponse.builder()
                .versionId(view.getVersionId())
                .versionNo(view.getVersionNo())
                .taskType(view.getTaskType())
                .status(view.getStatus())
                .sourceContentType(view.getSourceContentType())
                .sourceContentId(view.getSourceContentId())
                .sourceCategoryCode(view.getSourceCategoryCode())
                .sourceCategoryName(view.getSourceCategoryName())
                .appliedAt(view.getAppliedAt())
                .build();
    }

    private static KnowledgePortalLineageResponse.SummaryResponse toSummaryResponse(
            LineageCanvasResult.SummaryView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.SummaryResponse.builder()
                .nodeCount(view.getNodeCount())
                .relationCount(view.getRelationCount())
                .confirmedNodeCount(view.getConfirmedNodeCount())
                .confirmedRelationCount(view.getConfirmedRelationCount())
                .focusNodeId(view.getFocusNodeId())
                .focusRelationId(view.getFocusRelationId())
                .build();
    }

    private static KnowledgePortalLineageResponse.NodeResponse toNodeResponse(LineageCanvasResult.NodeView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.NodeResponse.builder()
                .id(view.getId())
                .nodeId(view.getNodeId())
                .nodeKey(view.getNodeKey())
                .name(view.getName())
                .nodeType(view.getNodeType())
                .generation(view.getGeneration())
                .gender(view.getGender())
                .confirmationStatus(view.getConfirmationStatus())
                .confidence(view.getConfidence())
                .sourceRefsJson(view.getSourceRefsJson())
                .sourceRefs(view.getSourceRefs().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toSourceRefResponse)
                        .toList())
                .firstExtractedAt(view.getFirstExtractedAt())
                .lastExtractedAt(view.getLastExtractedAt())
                .x(view.getX())
                .y(view.getY())
                .build();
    }

    private static KnowledgePortalLineageResponse.RelationResponse toRelationResponse(
            LineageCanvasResult.RelationView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.RelationResponse.builder()
                .id(view.getId())
                .relationId(view.getRelationId())
                .sourceNodeId(view.getSourceNodeId())
                .sourceNodeName(view.getSourceNodeName())
                .targetNodeId(view.getTargetNodeId())
                .targetNodeName(view.getTargetNodeName())
                .relationType(view.getRelationType())
                .relationLabel(view.getRelationLabel())
                .confirmationStatus(view.getConfirmationStatus())
                .confidence(view.getConfidence())
                .sourceRefsJson(view.getSourceRefsJson())
                .sourceRefs(view.getSourceRefs().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toSourceRefResponse)
                        .toList())
                .firstExtractedAt(view.getFirstExtractedAt())
                .lastExtractedAt(view.getLastExtractedAt())
                .build();
    }

    private static KnowledgePortalLineageResponse.SourceRefResponse toSourceRefResponse(
            LineageCanvasResult.SourceRefView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.SourceRefResponse.builder()
                .sourceContentType(view.getSourceContentType())
                .sourceContentId(view.getSourceContentId())
                .sourceTitle(view.getSourceTitle())
                .snippet(view.getSnippet())
                .href(view.getHref())
                .build();
    }

    private static KnowledgePortalLineageResponse.AvailableFiltersResponse toAvailableFiltersResponse(
            LineageCanvasResult.AvailableFiltersView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.AvailableFiltersResponse.builder()
                .versions(view.getVersions().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toVersionResponse)
                        .toList())
                .nodeTypes(view.getNodeTypes())
                .relationTypes(view.getRelationTypes())
                .confirmationStatuses(view.getConfirmationStatuses())
                .build();
    }

    private static KnowledgePortalLineageResponse.EmptyResponse toEmptyResponse(LineageCanvasResult.EmptyView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalLineageResponse.EmptyResponse.builder()
                .reason(view.getReason())
                .title(view.getTitle())
                .description(view.getDescription())
                .actionLabel(view.getActionLabel())
                .actionHref(view.getActionHref())
                .build();
    }
}
