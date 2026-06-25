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
        response.setFocusNode(toFocusNode(result.getFocusNode()));
        response.setRelationGroups(toRelationGroups(result.getRelationGroups()));
        response.setSourceReferences(toSourceReferences(result.getSourceReferences()));
        response.setRelatedTags(toRelatedTags(result.getRelatedTags()));
        response.setTimelineItems(toTimelineItems(result.getTimelineItems()));
        response.setAvailableFilters(toAvailableFilters(result.getAvailableFilters()));
        return response;
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
