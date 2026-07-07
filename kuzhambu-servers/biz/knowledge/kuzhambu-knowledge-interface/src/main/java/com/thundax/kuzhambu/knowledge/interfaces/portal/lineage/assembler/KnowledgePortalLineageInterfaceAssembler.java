package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.assembler;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.KnowledgePortalLineageController;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.response.KnowledgePortalLineageResponse;

public final class KnowledgePortalLineageInterfaceAssembler {

    private KnowledgePortalLineageInterfaceAssembler() {}

    public static LineageCanvasQuery toQuery(KnowledgePortalLineageController.Query request) {
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

    public static KnowledgePortalLineageResponse toResponse(LineageCanvasResult result) {
        if (result == null) {
            return new KnowledgePortalLineageResponse();
        }
        return new KnowledgePortalLineageResponse(
                toVersionResponse(result.getVersion()),
                toSummaryResponse(result.getSummary()),
                result.getNodes().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toNodeResponse)
                        .toList(),
                result.getRelations().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toRelationResponse)
                        .toList(),
                toNodeResponse(result.getSelectedNode()),
                toRelationResponse(result.getSelectedRelation()),
                toAvailableFiltersResponse(result.getAvailableFilters()),
                toEmptyResponse(result.getEmpty()));
    }

    private static KnowledgePortalLineageResponse.VersionResponse toVersionResponse(
            LineageCanvasResult.VersionView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.VersionResponse(
                view.getVersionId(),
                view.getVersionNo(),
                view.getTaskType(),
                view.getStatus(),
                view.getSourceContentType(),
                view.getSourceContentId(),
                view.getSourceCategoryCode(),
                view.getSourceCategoryName(),
                view.getAppliedAt());
    }

    private static KnowledgePortalLineageResponse.VersionResponse toVersionResponse(
            LineageCanvasResult.VersionOptionView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.VersionResponse(
                view.getVersionId(),
                view.getVersionNo(),
                view.getTaskType(),
                view.getStatus(),
                view.getSourceContentType(),
                view.getSourceContentId(),
                view.getSourceCategoryCode(),
                view.getSourceCategoryName(),
                view.getAppliedAt());
    }

    private static KnowledgePortalLineageResponse.SummaryResponse toSummaryResponse(
            LineageCanvasResult.SummaryView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.SummaryResponse(
                view.getNodeCount(),
                view.getRelationCount(),
                view.getConfirmedNodeCount(),
                view.getConfirmedRelationCount(),
                view.getFocusNodeId(),
                view.getFocusRelationId());
    }

    private static KnowledgePortalLineageResponse.NodeResponse toNodeResponse(LineageCanvasResult.NodeView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.NodeResponse(
                view.getId(),
                view.getNodeId(),
                view.getNodeKey(),
                view.getName(),
                view.getNodeType(),
                view.getGeneration(),
                view.getGender(),
                view.getConfirmationStatus(),
                view.getConfidence(),
                view.getSourceRefsJson(),
                view.getSourceRefs().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toSourceRefResponse)
                        .toList(),
                view.getFirstExtractedAt(),
                view.getLastExtractedAt(),
                view.getX(),
                view.getY());
    }

    private static KnowledgePortalLineageResponse.RelationResponse toRelationResponse(
            LineageCanvasResult.RelationView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.RelationResponse(
                view.getId(),
                view.getRelationId(),
                view.getSourceNodeId(),
                view.getSourceNodeName(),
                view.getTargetNodeId(),
                view.getTargetNodeName(),
                view.getRelationType(),
                view.getRelationLabel(),
                view.getConfirmationStatus(),
                view.getConfidence(),
                view.getSourceRefsJson(),
                view.getSourceRefs().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toSourceRefResponse)
                        .toList(),
                view.getFirstExtractedAt(),
                view.getLastExtractedAt());
    }

    private static KnowledgePortalLineageResponse.SourceRefResponse toSourceRefResponse(
            LineageCanvasResult.SourceRefView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.SourceRefResponse(
                view.getSourceContentType(),
                view.getSourceContentId(),
                view.getSourceTitle(),
                view.getSnippet(),
                view.getHref());
    }

    private static KnowledgePortalLineageResponse.AvailableFiltersResponse toAvailableFiltersResponse(
            LineageCanvasResult.AvailableFiltersView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.AvailableFiltersResponse(
                view.getVersions().stream()
                        .map(KnowledgePortalLineageInterfaceAssembler::toVersionResponse)
                        .toList(),
                view.getNodeTypes(),
                view.getRelationTypes(),
                view.getConfirmationStatuses());
    }

    private static KnowledgePortalLineageResponse.EmptyResponse toEmptyResponse(LineageCanvasResult.EmptyView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalLineageResponse.EmptyResponse(
                view.getReason(), view.getTitle(), view.getDescription(), view.getActionLabel(), view.getActionHref());
    }
}
