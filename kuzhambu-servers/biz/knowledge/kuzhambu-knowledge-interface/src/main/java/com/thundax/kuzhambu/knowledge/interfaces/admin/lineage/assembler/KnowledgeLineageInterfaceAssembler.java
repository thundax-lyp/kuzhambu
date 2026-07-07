package com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.assembler;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.request.LineageCanvasRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.response.LineageCanvasResponse;

public final class KnowledgeLineageInterfaceAssembler {

    private KnowledgeLineageInterfaceAssembler() {}

    public static LineageCanvasQuery toQuery(LineageCanvasRequest request) {
        return new LineageCanvasQuery(
                request == null ? null : request.getVersionId(),
                request == null ? null : request.getFocusNodeId(),
                request == null ? null : request.getFocusRelationId(),
                request == null ? null : request.getKeyword(),
                request == null ? null : request.getNodeType(),
                request == null ? null : request.getRelationType(),
                request == null ? null : request.getConfirmationStatus(),
                request == null ? null : request.getDepth());
    }

    public static LineageCanvasResponse toResponse(LineageCanvasResult result) {
        return LineageCanvasResponse.builder()
                .version(toVersionResponse(result == null ? null : result.getVersion()))
                .summary(toSummaryResponse(result == null ? null : result.getSummary()))
                .nodes(
                        result == null
                                ? null
                                : result.getNodes().stream()
                                        .map(KnowledgeLineageInterfaceAssembler::toNodeResponse)
                                        .toList())
                .relations(
                        result == null
                                ? null
                                : result.getRelations().stream()
                                        .map(KnowledgeLineageInterfaceAssembler::toRelationResponse)
                                        .toList())
                .selectedNode(toNodeResponse(result == null ? null : result.getSelectedNode()))
                .selectedRelation(toRelationResponse(result == null ? null : result.getSelectedRelation()))
                .availableFilters(toAvailableFiltersResponse(result == null ? null : result.getAvailableFilters()))
                .empty(toEmptyResponse(result == null ? null : result.getEmpty()))
                .build();
    }

    private static LineageCanvasResponse.VersionResponse toVersionResponse(LineageCanvasResult.VersionView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.VersionResponse.builder()
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

    private static LineageCanvasResponse.VersionResponse toVersionResponse(LineageCanvasResult.VersionOptionView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.VersionResponse.builder()
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

    private static LineageCanvasResponse.SummaryResponse toSummaryResponse(LineageCanvasResult.SummaryView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.SummaryResponse.builder()
                .nodeCount(view.getNodeCount())
                .relationCount(view.getRelationCount())
                .confirmedNodeCount(view.getConfirmedNodeCount())
                .confirmedRelationCount(view.getConfirmedRelationCount())
                .focusNodeId(view.getFocusNodeId())
                .focusRelationId(view.getFocusRelationId())
                .build();
    }

    private static LineageCanvasResponse.NodeResponse toNodeResponse(LineageCanvasResult.NodeView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.NodeResponse.builder()
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
                        .map(KnowledgeLineageInterfaceAssembler::toSourceRefResponse)
                        .toList())
                .firstExtractedAt(view.getFirstExtractedAt())
                .lastExtractedAt(view.getLastExtractedAt())
                .x(view.getX())
                .y(view.getY())
                .build();
    }

    private static LineageCanvasResponse.RelationResponse toRelationResponse(LineageCanvasResult.RelationView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.RelationResponse.builder()
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
                        .map(KnowledgeLineageInterfaceAssembler::toSourceRefResponse)
                        .toList())
                .firstExtractedAt(view.getFirstExtractedAt())
                .lastExtractedAt(view.getLastExtractedAt())
                .build();
    }

    private static LineageCanvasResponse.SourceRefResponse toSourceRefResponse(LineageCanvasResult.SourceRefView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.SourceRefResponse.builder()
                .sourceContentType(view.getSourceContentType())
                .sourceContentId(view.getSourceContentId())
                .sourceTitle(view.getSourceTitle())
                .snippet(view.getSnippet())
                .href(view.getHref())
                .build();
    }

    private static LineageCanvasResponse.AvailableFiltersResponse toAvailableFiltersResponse(
            LineageCanvasResult.AvailableFiltersView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.AvailableFiltersResponse.builder()
                .versions(view.getVersions().stream()
                        .map(KnowledgeLineageInterfaceAssembler::toVersionResponse)
                        .toList())
                .nodeTypes(view.getNodeTypes())
                .relationTypes(view.getRelationTypes())
                .confirmationStatuses(view.getConfirmationStatuses())
                .build();
    }

    private static LineageCanvasResponse.EmptyResponse toEmptyResponse(LineageCanvasResult.EmptyView view) {
        if (view == null) {
            return null;
        }
        return LineageCanvasResponse.EmptyResponse.builder()
                .reason(view.getReason())
                .title(view.getTitle())
                .description(view.getDescription())
                .actionLabel(view.getActionLabel())
                .actionHref(view.getActionHref())
                .build();
    }
}
