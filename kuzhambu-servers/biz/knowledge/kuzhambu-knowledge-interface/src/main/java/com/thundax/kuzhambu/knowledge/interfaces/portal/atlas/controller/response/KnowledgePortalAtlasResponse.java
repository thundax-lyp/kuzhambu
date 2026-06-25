package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalAtlasResponse {
    private String currentLevel;
    private List<BreadcrumbItemResponse> breadcrumbItems;
    private OverviewViewResponse overviewView;
    private CategoryViewResponse categoryView;
    private DetailViewResponse detailView;
    private AvailableFiltersResponse availableFilters;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbItemResponse {
        private String level;
        private String label;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewViewResponse {
        private String summaryTitle;
        private String summarySubtitle;
        private List<OverviewCategoryCardResponse> categoryCards;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewCategoryCardResponse {
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
    public static class CategoryViewResponse {
        private String categoryCode;
        private String categoryName;
        private Long latestVersionId;
        private Integer latestVersionNo;
        private List<CategoryEntityHighlightResponse> entityHighlights;
        private List<RelationGroupResponse> relationGroups;
        private List<SourceReferenceResponse> sourceReferences;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEntityHighlightResponse {
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
    public static class DetailViewResponse {
        private FocusNodeResponse focusNode;
        private List<RelationGroupResponse> relationGroups;
        private List<SourceReferenceResponse> sourceReferences;
        private List<TimelineItemResponse> timelineItems;
        private List<RelatedTagResponse> relatedTags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusNodeResponse {
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
    public static class RelationGroupResponse {
        private String groupKey;
        private String groupLabel;
        private List<RelationItemResponse> relations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationItemResponse {
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
    public static class SourceReferenceResponse {
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
    public static class RelatedTagResponse {
        private String tagId;
        private String tagName;
        private String tagCategory;
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItemResponse {
        private String timeLabel;
        private String title;
        private String description;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableFiltersResponse {
        private List<String> knowledgeBases;
        private List<String> entityTypes;
        private List<String> relationTypes;
        private List<String> tagNames;
        private List<String> timeRanges;
    }
}
