package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "KnowledgePortalAtlasResponse", description = "Atlas 知识门户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalAtlasResponse {
    private String currentLevel;
    private List<BreadcrumbItemResponse> breadcrumbItems;
    private OverviewViewResponse overviewView;
    private CategoryViewResponse categoryView;
    private DetailViewResponse detailView;
    private AvailableFiltersResponse availableFilters;
    private CanvasViewResponse canvasView;

    @Getter
    @Builder
    @Schema(name = "BreadcrumbItemResponse", description = "面包屑项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BreadcrumbItemResponse {
        private String level;
        private String label;
        private String href;
    }

    @Getter
    @Builder
    @Schema(name = "OverviewViewResponse", description = "Atlas overview 视图")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverviewViewResponse {
        private String summaryTitle;
        private String summarySubtitle;
        private List<OverviewCategoryCardResponse> categoryCards;
    }

    @Getter
    @Builder
    @Schema(name = "OverviewCategoryCardResponse", description = "Atlas 分类卡片")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @Builder
    @Schema(name = "CategoryViewResponse", description = "Atlas 分类视图")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @Builder
    @Schema(name = "CategoryEntityHighlightResponse", description = "Atlas 分类实体高亮")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryEntityHighlightResponse {
        private String entityId;
        private String entityName;
        private String entityType;
        private String confirmationStatus;
        private String entryHref;
    }

    @Getter
    @Builder
    @Schema(name = "DetailViewResponse", description = "Atlas 详情视图")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailViewResponse {
        private FocusNodeResponse focusNode;
        private List<RelationGroupResponse> relationGroups;
        private List<SourceReferenceResponse> sourceReferences;
        private List<TimelineItemResponse> timelineItems;
        private List<RelatedTagResponse> relatedTags;
    }

    @Getter
    @Builder
    @Schema(name = "FocusNodeResponse", description = "Atlas 聚焦节点")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @Builder
    @Schema(name = "RelationGroupResponse", description = "Atlas 关系分组")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationGroupResponse {
        private String groupKey;
        private String groupLabel;
        private List<RelationItemResponse> relations;
    }

    @Getter
    @Builder
    @Schema(name = "RelationItemResponse", description = "Atlas 关系项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @Builder
    @Schema(name = "SourceReferenceResponse", description = "Atlas 来源引用")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceReferenceResponse {
        private String sourceId;
        private String sourceTitle;
        private String sourceType;
        private String snippet;
        private Long updatedAt;
        private String href;
    }

    @Getter
    @Builder
    @Schema(name = "RelatedTagResponse", description = "Atlas 关联标签")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelatedTagResponse {
        private String tagId;
        private String tagName;
        private String tagCategory;
        private Double score;
    }

    @Getter
    @Builder
    @Schema(name = "TimelineItemResponse", description = "Atlas 时间轴条目")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimelineItemResponse {
        private String timeLabel;
        private String title;
        private String description;
        private String href;
    }

    @Getter
    @Builder
    @Schema(name = "AvailableFiltersResponse", description = "Atlas 过滤条件")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvailableFiltersResponse {
        private List<String> knowledgeBases;
        private List<String> entityTypes;
        private List<String> relationTypes;
        private List<String> tagNames;
        private List<String> timeRanges;
    }

    @Getter
    @Builder
    @Schema(name = "CanvasViewResponse", description = "Atlas 画布视图")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CanvasViewResponse {
        private String mode;
        private String title;
        private String description;
        private String focusNodeId;
        private Boolean empty;
        private String emptyTitle;
        private String emptyDescription;
        private List<CanvasNodeResponse> nodes;
        private List<CanvasEdgeResponse> edges;
    }

    @Getter
    @Builder
    @Schema(name = "CanvasNodeResponse", description = "Atlas 画布节点")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CanvasNodeResponse {
        private String id;
        private String kind;
        private String label;
        private String subtitle;
        private String metricLabel;
        private Long metricValue;
        private String status;
        private String categoryCode;
        private Long entityId;
        private String href;
        private Double weight;
        private Double x;
        private Double y;
    }

    @Getter
    @Builder
    @Schema(name = "CanvasEdgeResponse", description = "Atlas 画布边")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CanvasEdgeResponse {
        private String id;
        private String source;
        private String target;
        private String label;
        private String relationType;
        private Double weight;
        private Boolean dashed;
    }
}
