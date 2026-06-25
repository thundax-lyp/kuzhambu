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
    private FocusNode focusNode;
    private List<RelationGroup> relationGroups;
    private List<SourceReference> sourceReferences;
    private List<RelatedTag> relatedTags;
    private List<TimelineItem> timelineItems;
    private AvailableFilters availableFilters;

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
