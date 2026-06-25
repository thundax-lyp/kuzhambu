package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler;

import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import java.util.Collections;
import java.util.List;

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
        return response;
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
}
