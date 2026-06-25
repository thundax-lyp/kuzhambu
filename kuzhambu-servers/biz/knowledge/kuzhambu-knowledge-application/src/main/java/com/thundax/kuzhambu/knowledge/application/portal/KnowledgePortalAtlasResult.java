package com.thundax.kuzhambu.knowledge.application.portal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalAtlasResult {
    private String currentLevel;
    private List<BreadcrumbItem> breadcrumbItems;
    private OverviewView overviewView;
    private CategoryView categoryView;
    private DetailView detailView;
    private AvailableFilters availableFilters;

    public KnowledgePortalAtlasResult(
            FocusNode focusNode,
            List<RelationGroup> relationGroups,
            List<SourceReference> sourceReferences,
            List<RelatedTag> relatedTags,
            List<TimelineItem> timelineItems,
            AvailableFilters availableFilters) {
        this.currentLevel = "detail";
        this.breadcrumbItems = List.of();
        this.overviewView = null;
        this.categoryView = null;
        this.detailView = new DetailView(focusNode, relationGroups, sourceReferences, timelineItems, relatedTags);
        this.availableFilters = availableFilters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbItem {
        private String level;
        private String label;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewView {
        private String summaryTitle;
        private String summarySubtitle;
        private List<OverviewCategoryCard> categoryCards;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewCategoryCard {
        private String categoryCode;
        private String categoryName;
        private Long entityCount;
        private Long relationCount;
        private Long appliedVersionCount;
        private Integer latestVersionNo;
        private String entryHref;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryView {
        private String categoryCode;
        private String categoryName;
        private Long latestVersionId;
        private Integer latestVersionNo;
        private List<CategoryEntityHighlight> entityHighlights;
        private List<RelationGroup> relationGroups;
        private List<SourceReference> sourceReferences;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEntityHighlight {
        private String entityId;
        private String entityName;
        private String entityType;
        private String confirmationStatus;
        private String entryHref;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailView {
        private FocusNode focusNode;
        private List<RelationGroup> relationGroups;
        private List<SourceReference> sourceReferences;
        private List<TimelineItem> timelineItems;
        private List<RelatedTag> relatedTags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusNode {
        private String id;
        private String title;
        private String type;
        private String summary;
        private String status;
        private Double confidence;
        private String coverImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationGroup {
        private String groupKey;
        private String groupLabel;
        private List<RelationItem> relations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationItem {
        private String sourceId;
        private String sourceLabel;
        private String relationLabel;
        private String targetId;
        private String targetLabel;
        private String relationType;
        private Double weight;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceReference {
        private String sourceId;
        private String sourceTitle;
        private String sourceType;
        private String snippet;
        private Long updatedAt;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedTag {
        private String tagId;
        private String tagName;
        private String tagCategory;
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {
        private String timeLabel;
        private String title;
        private String description;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableFilters {
        private List<String> knowledgeBases;
        private List<String> entityTypes;
        private List<String> relationTypes;
        private List<String> tagNames;
        private List<String> timeRanges;
    }
}
