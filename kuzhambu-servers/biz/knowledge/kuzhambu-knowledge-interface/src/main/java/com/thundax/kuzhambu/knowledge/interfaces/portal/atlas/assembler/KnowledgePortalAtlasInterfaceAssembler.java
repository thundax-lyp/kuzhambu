package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler;

import com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasRequest;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgePortalAtlasInterfaceAssembler {

    private KnowledgePortalAtlasInterfaceAssembler() {}

    public static KnowledgePortalAtlasResponse toResponse(KnowledgePortalAtlasResult result) {
        if (result == null) {
            return null;
        }
        KnowledgePortalAtlasResponse response = new KnowledgePortalAtlasResponse();
        response.setCurrentLevel(result.getCurrentLevel());
        response.setBreadcrumbItems(toBreadcrumbItems(result.getBreadcrumbItems()));
        response.setOverviewView(toOverviewView(result.getOverviewView()));
        response.setCategoryView(toCategoryView(result.getCategoryView()));
        response.setDetailView(toDetailView(result.getDetailView()));
        response.setAvailableFilters(toAvailableFilters(result.getAvailableFilters()));
        response.setCanvasView(toCanvasView(result.getCanvasView()));
        return response;
    }

    @NonNull
    public static KnowledgePortalAtlasQuery toAtlasQuery(@NonNull KnowledgePortalAtlasRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new KnowledgePortalAtlasQuery(
                request.getLevel(),
                request.getCategoryCode(),
                request.getEntityId(),
                request.getKnowledgeBase(),
                request.getKeyword(),
                request.getTag(),
                request.getTimeRange());
    }

    private static List<KnowledgePortalAtlasResponse.BreadcrumbItemResponse> toBreadcrumbItems(
            List<KnowledgePortalAtlasResult.BreadcrumbItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> new KnowledgePortalAtlasResponse.BreadcrumbItemResponse(
                        item.getLevel(), item.getLabel(), item.getHref()))
                .toList();
    }

    private static KnowledgePortalAtlasResponse.OverviewViewResponse toOverviewView(
            KnowledgePortalAtlasResult.OverviewView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.OverviewViewResponse(
                view.getSummaryTitle(), view.getSummarySubtitle(), toOverviewCategoryCards(view.getCategoryCards()));
    }

    private static List<KnowledgePortalAtlasResponse.OverviewCategoryCardResponse> toOverviewCategoryCards(
            List<KnowledgePortalAtlasResult.OverviewCategoryCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        return cards.stream()
                .map(card -> new KnowledgePortalAtlasResponse.OverviewCategoryCardResponse(
                        card.getCategoryCode(),
                        card.getCategoryName(),
                        card.getEntityCount(),
                        card.getRelationCount(),
                        card.getAppliedVersionCount(),
                        card.getLatestVersionNo(),
                        card.getEntryHref()))
                .toList();
    }

    private static KnowledgePortalAtlasResponse.CategoryViewResponse toCategoryView(
            KnowledgePortalAtlasResult.CategoryView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.CategoryViewResponse(
                view.getCategoryCode(),
                view.getCategoryName(),
                view.getLatestVersionId(),
                view.getLatestVersionNo(),
                toCategoryEntityHighlights(view.getEntityHighlights()),
                toRelationGroups(view.getRelationGroups()),
                toSourceReferences(view.getSourceReferences()));
    }

    private static List<KnowledgePortalAtlasResponse.CategoryEntityHighlightResponse> toCategoryEntityHighlights(
            List<KnowledgePortalAtlasResult.CategoryEntityHighlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return Collections.emptyList();
        }
        return highlights.stream()
                .map(item -> new KnowledgePortalAtlasResponse.CategoryEntityHighlightResponse(
                        item.getEntityId(),
                        item.getEntityName(),
                        item.getEntityType(),
                        item.getConfirmationStatus(),
                        item.getEntryHref()))
                .toList();
    }

    private static KnowledgePortalAtlasResponse.DetailViewResponse toDetailView(
            KnowledgePortalAtlasResult.DetailView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.DetailViewResponse(
                toFocusNode(view.getFocusNode()),
                toRelationGroups(view.getRelationGroups()),
                toSourceReferences(view.getSourceReferences()),
                toTimelineItems(view.getTimelineItems()),
                toRelatedTags(view.getRelatedTags()));
    }

    private static KnowledgePortalAtlasResponse.FocusNodeResponse toFocusNode(
            KnowledgePortalAtlasResult.FocusNode node) {
        if (node == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.FocusNodeResponse(
                node.getId(),
                node.getTitle(),
                node.getType(),
                node.getSummary(),
                node.getStatus(),
                node.getConfidence(),
                node.getCoverImageUrl());
    }

    private static List<KnowledgePortalAtlasResponse.RelationGroupResponse> toRelationGroups(
            List<KnowledgePortalAtlasResult.RelationGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream()
                .map(group -> new KnowledgePortalAtlasResponse.RelationGroupResponse(
                        group.getGroupKey(), group.getGroupLabel(), toRelationItems(group.getRelations())))
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.RelationItemResponse> toRelationItems(
            List<KnowledgePortalAtlasResult.RelationItem> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        return relations.stream()
                .map(relation -> new KnowledgePortalAtlasResponse.RelationItemResponse(
                        relation.getSourceId(),
                        relation.getSourceLabel(),
                        relation.getRelationLabel(),
                        relation.getTargetId(),
                        relation.getTargetLabel(),
                        relation.getRelationType(),
                        relation.getWeight()))
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.SourceReferenceResponse> toSourceReferences(
            List<KnowledgePortalAtlasResult.SourceReference> sourceReferences) {
        if (sourceReferences == null || sourceReferences.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceReferences.stream()
                .map(reference -> new KnowledgePortalAtlasResponse.SourceReferenceResponse(
                        reference.getSourceId(),
                        reference.getSourceTitle(),
                        reference.getSourceType(),
                        reference.getSnippet(),
                        reference.getUpdatedAt(),
                        reference.getHref()))
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.RelatedTagResponse> toRelatedTags(
            List<KnowledgePortalAtlasResult.RelatedTag> relatedTags) {
        if (relatedTags == null || relatedTags.isEmpty()) {
            return Collections.emptyList();
        }
        return relatedTags.stream()
                .map(tag -> new KnowledgePortalAtlasResponse.RelatedTagResponse(
                        tag.getTagId(), tag.getTagName(), tag.getTagCategory(), tag.getScore()))
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.TimelineItemResponse> toTimelineItems(
            List<KnowledgePortalAtlasResult.TimelineItem> timelineItems) {
        if (timelineItems == null || timelineItems.isEmpty()) {
            return Collections.emptyList();
        }
        return timelineItems.stream()
                .map(item -> new KnowledgePortalAtlasResponse.TimelineItemResponse(
                        item.getTimeLabel(), item.getTitle(), item.getDescription(), item.getHref()))
                .toList();
    }

    private static KnowledgePortalAtlasResponse.AvailableFiltersResponse toAvailableFilters(
            KnowledgePortalAtlasResult.AvailableFilters filters) {
        if (filters == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.AvailableFiltersResponse(
                filters.getKnowledgeBases(),
                filters.getEntityTypes(),
                filters.getRelationTypes(),
                filters.getTagNames(),
                filters.getTimeRanges());
    }

    private static KnowledgePortalAtlasResponse.CanvasViewResponse toCanvasView(
            KnowledgePortalAtlasResult.CanvasView view) {
        if (view == null) {
            return null;
        }
        return new KnowledgePortalAtlasResponse.CanvasViewResponse(
                view.getMode(),
                view.getTitle(),
                view.getDescription(),
                view.getFocusNodeId(),
                view.getEmpty(),
                view.getEmptyTitle(),
                view.getEmptyDescription(),
                toCanvasNodes(view.getNodes()),
                toCanvasEdges(view.getEdges()));
    }

    private static List<KnowledgePortalAtlasResponse.CanvasNodeResponse> toCanvasNodes(
            List<KnowledgePortalAtlasResult.CanvasNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        return nodes.stream()
                .map(node -> new KnowledgePortalAtlasResponse.CanvasNodeResponse(
                        node.getId(),
                        node.getKind(),
                        node.getLabel(),
                        node.getSubtitle(),
                        node.getMetricLabel(),
                        node.getMetricValue(),
                        node.getStatus(),
                        node.getCategoryCode(),
                        node.getEntityId(),
                        node.getHref(),
                        node.getWeight(),
                        node.getX(),
                        node.getY()))
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.CanvasEdgeResponse> toCanvasEdges(
            List<KnowledgePortalAtlasResult.CanvasEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        return edges.stream()
                .map(edge -> new KnowledgePortalAtlasResponse.CanvasEdgeResponse(
                        edge.getId(),
                        edge.getSource(),
                        edge.getTarget(),
                        edge.getLabel(),
                        edge.getRelationType(),
                        edge.getWeight(),
                        edge.getDashed()))
                .toList();
    }
}
