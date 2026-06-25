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
public class KnowledgePortalHomeResult {
    private String heroTitle;
    private String heroSubtitle;
    private String searchPlaceholder;
    private List<PortalStatItem> stats;
    private List<PortalQuickLinkItem> quickLinks;
    private List<PortalRecentUpdateItem> recentUpdates;
    private List<PortalFeatureCollectionItem> featureCollections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortalStatItem {
        private String key;
        private String label;
        private String value;
        private String deltaText;
        private String trend;
        private String icon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortalQuickLinkItem {
        private String key;
        private String label;
        private String description;
        private String href;
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortalRecentUpdateItem {
        private String title;
        private String subtitle;
        private String summary;
        private Long updatedAt;
        private String href;
        private String coverImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortalFeatureCollectionItem {
        private String key;
        private String label;
        private String description;
        private String href;
        private String badgeText;
    }
}
