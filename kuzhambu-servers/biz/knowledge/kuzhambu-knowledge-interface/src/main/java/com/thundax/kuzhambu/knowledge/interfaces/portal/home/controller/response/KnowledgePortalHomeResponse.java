package com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalHomeResponse {
    private String heroTitle;
    private String heroSubtitle;
    private String searchPlaceholder;
    private List<PortalStatResponse> stats;
    private List<PortalQuickLinkResponse> quickLinks;
    private List<PortalRecentUpdateResponse> recentUpdates;
    private List<PortalFeatureCollectionResponse> featureCollections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortalStatResponse {
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
    public static class PortalQuickLinkResponse {
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
    public static class PortalRecentUpdateResponse {
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
    public static class PortalFeatureCollectionResponse {
        private String key;
        private String label;
        private String description;
        private String href;
        private String badgeText;
    }
}
