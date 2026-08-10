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

    @NonNull
    public static KnowledgePortalAtlasResponse toResponse(@NonNull KnowledgePortalAtlasResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgePortalAtlasResponse.builder()
                .currentLevel(result.getCurrentLevel())
                .breadcrumbItems(toBreadcrumbItems(result.getBreadcrumbItems()))
                .overviewView(toOverviewView(result.getOverviewView()))
                .categoryView(toCategoryView(result.getCategoryView()))
                .detailView(toDetailView(result.getDetailView()))
                .availableFilters(toAvailableFilters(result.getAvailableFilters()))
                .canvasView(toCanvasView(result.getCanvasView()))
                .build();
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
                .map(item -> KnowledgePortalAtlasResponse.BreadcrumbItemResponse.builder()
                        .level(item.getLevel())
                        .label(item.getLabel())
                        .href(item.getHref())
                        .build())
                .toList();
    }

    private static KnowledgePortalAtlasResponse.OverviewViewResponse toOverviewView(
            KnowledgePortalAtlasResult.OverviewView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.OverviewViewResponse.builder()
                .summaryTitle(view.getSummaryTitle())
                .summarySubtitle(view.getSummarySubtitle())
                .categoryCards(toOverviewCategoryCards(view.getCategoryCards()))
                .build();
    }

    private static List<KnowledgePortalAtlasResponse.OverviewCategoryCardResponse> toOverviewCategoryCards(
            List<KnowledgePortalAtlasResult.OverviewCategoryCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        return cards.stream()
                .map(card -> KnowledgePortalAtlasResponse.OverviewCategoryCardResponse.builder()
                        .categoryCode(card.getCategoryCode())
                        .categoryName(card.getCategoryName())
                        .entityCount(card.getEntityCount())
                        .relationCount(card.getRelationCount())
                        .appliedVersionCount(card.getAppliedVersionCount())
                        .latestVersionNo(card.getLatestVersionNo())
                        .entryHref(card.getEntryHref())
                        .build())
                .toList();
    }

    private static KnowledgePortalAtlasResponse.CategoryViewResponse toCategoryView(
            KnowledgePortalAtlasResult.CategoryView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.CategoryViewResponse.builder()
                .categoryCode(view.getCategoryCode())
                .categoryName(view.getCategoryName())
                .latestVersionId(view.getLatestVersionId())
                .latestVersionNo(view.getLatestVersionNo())
                .entityHighlights(toCategoryEntityHighlights(view.getEntityHighlights()))
                .relationGroups(toRelationGroups(view.getRelationGroups()))
                .sourceReferences(toSourceReferences(view.getSourceReferences()))
                .build();
    }

    private static List<KnowledgePortalAtlasResponse.CategoryEntityHighlightResponse> toCategoryEntityHighlights(
            List<KnowledgePortalAtlasResult.CategoryEntityHighlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return Collections.emptyList();
        }
        return highlights.stream()
                .map(item -> KnowledgePortalAtlasResponse.CategoryEntityHighlightResponse.builder()
                        .entityId(item.getEntityId())
                        .entityName(item.getEntityName())
                        .entityType(item.getEntityType())
                        .confirmationStatus(item.getConfirmationStatus())
                        .entryHref(item.getEntryHref())
                        .build())
                .toList();
    }

    private static KnowledgePortalAtlasResponse.DetailViewResponse toDetailView(
            KnowledgePortalAtlasResult.DetailView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.DetailViewResponse.builder()
                .focusNode(toFocusNode(view.getFocusNode()))
                .relationGroups(toRelationGroups(view.getRelationGroups()))
                .sourceReferences(toSourceReferences(view.getSourceReferences()))
                .timelineItems(toTimelineItems(view.getTimelineItems()))
                .relatedTags(toRelatedTags(view.getRelatedTags()))
                .build();
    }

    private static KnowledgePortalAtlasResponse.FocusNodeResponse toFocusNode(
            KnowledgePortalAtlasResult.FocusNode node) {
        if (node == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.FocusNodeResponse.builder()
                .id(node.getId())
                .title(node.getTitle())
                .type(node.getType())
                .summary(node.getSummary())
                .status(node.getStatus())
                .confidence(node.getConfidence())
                .coverImageUrl(node.getCoverImageUrl())
                .build();
    }

    private static List<KnowledgePortalAtlasResponse.RelationGroupResponse> toRelationGroups(
            List<KnowledgePortalAtlasResult.RelationGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream()
                .map(group -> KnowledgePortalAtlasResponse.RelationGroupResponse.builder()
                        .groupKey(group.getGroupKey())
                        .groupLabel(group.getGroupLabel())
                        .relations(toRelationItems(group.getRelations()))
                        .build())
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.RelationItemResponse> toRelationItems(
            List<KnowledgePortalAtlasResult.RelationItem> relations) {
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        return relations.stream()
                .map(relation -> KnowledgePortalAtlasResponse.RelationItemResponse.builder()
                        .sourceId(relation.getSourceId())
                        .sourceLabel(relation.getSourceLabel())
                        .relationLabel(relation.getRelationLabel())
                        .targetId(relation.getTargetId())
                        .targetLabel(relation.getTargetLabel())
                        .relationType(relation.getRelationType())
                        .weight(relation.getWeight())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.SourceReferenceResponse> toSourceReferences(
            List<KnowledgePortalAtlasResult.SourceReference> sourceReferences) {
        if (sourceReferences == null || sourceReferences.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceReferences.stream()
                .map(reference -> KnowledgePortalAtlasResponse.SourceReferenceResponse.builder()
                        .sourceId(reference.getSourceId())
                        .sourceTitle(reference.getSourceTitle())
                        .sourceType(reference.getSourceType())
                        .snippet(reference.getSnippet())
                        .updatedAt(reference.getUpdatedAt())
                        .href(reference.getHref())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.RelatedTagResponse> toRelatedTags(
            List<KnowledgePortalAtlasResult.RelatedTag> relatedTags) {
        if (relatedTags == null || relatedTags.isEmpty()) {
            return Collections.emptyList();
        }
        return relatedTags.stream()
                .map(tag -> KnowledgePortalAtlasResponse.RelatedTagResponse.builder()
                        .tagId(tag.getTagId())
                        .tagName(tag.getTagName())
                        .tagCategory(tag.getTagCategory())
                        .score(tag.getScore())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.TimelineItemResponse> toTimelineItems(
            List<KnowledgePortalAtlasResult.TimelineItem> timelineItems) {
        if (timelineItems == null || timelineItems.isEmpty()) {
            return Collections.emptyList();
        }
        return timelineItems.stream()
                .map(item -> KnowledgePortalAtlasResponse.TimelineItemResponse.builder()
                        .timeLabel(item.getTimeLabel())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .href(item.getHref())
                        .build())
                .toList();
    }

    private static KnowledgePortalAtlasResponse.AvailableFiltersResponse toAvailableFilters(
            KnowledgePortalAtlasResult.AvailableFilters filters) {
        if (filters == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.AvailableFiltersResponse.builder()
                .knowledgeBases(filters.getKnowledgeBases())
                .entityTypes(filters.getEntityTypes())
                .relationTypes(filters.getRelationTypes())
                .tagNames(filters.getTagNames())
                .timeRanges(filters.getTimeRanges())
                .build();
    }

    private static KnowledgePortalAtlasResponse.CanvasViewResponse toCanvasView(
            KnowledgePortalAtlasResult.CanvasView view) {
        if (view == null) {
            return null;
        }
        return KnowledgePortalAtlasResponse.CanvasViewResponse.builder()
                .mode(view.getMode())
                .title(view.getTitle())
                .description(view.getDescription())
                .focusNodeId(view.getFocusNodeId())
                .empty(view.getEmpty())
                .emptyTitle(view.getEmptyTitle())
                .emptyDescription(view.getEmptyDescription())
                .nodes(toCanvasNodes(view.getNodes()))
                .edges(toCanvasEdges(view.getEdges()))
                .build();
    }

    private static List<KnowledgePortalAtlasResponse.CanvasNodeResponse> toCanvasNodes(
            List<KnowledgePortalAtlasResult.CanvasNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        return nodes.stream()
                .map(node -> KnowledgePortalAtlasResponse.CanvasNodeResponse.builder()
                        .id(node.getId())
                        .kind(node.getKind())
                        .label(node.getLabel())
                        .subtitle(node.getSubtitle())
                        .metricLabel(node.getMetricLabel())
                        .metricValue(node.getMetricValue())
                        .status(node.getStatus())
                        .categoryCode(node.getCategoryCode())
                        .entityId(node.getEntityId())
                        .href(node.getHref())
                        .weight(node.getWeight())
                        .x(node.getX())
                        .y(node.getY())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalAtlasResponse.CanvasEdgeResponse> toCanvasEdges(
            List<KnowledgePortalAtlasResult.CanvasEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        return edges.stream()
                .map(edge -> KnowledgePortalAtlasResponse.CanvasEdgeResponse.builder()
                        .id(edge.getId())
                        .source(edge.getSource())
                        .target(edge.getTarget())
                        .label(edge.getLabel())
                        .relationType(edge.getRelationType())
                        .weight(edge.getWeight())
                        .dashed(edge.getDashed())
                        .build())
                .toList();
    }
}
