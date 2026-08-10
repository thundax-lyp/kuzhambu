package com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "KnowledgePortalHomeResponse", description = "首页门户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalHomeResponse {
    private String heroTitle;
    private String heroSubtitle;
    private String searchPlaceholder;
    private List<PortalStatResponse> stats;
    private List<PortalQuickLinkResponse> quickLinks;
    private List<PortalRecentUpdateResponse> recentUpdates;
    private List<PortalFeatureCollectionResponse> featureCollections;

    @Getter
    @Builder
    @Schema(name = "PortalStatResponse", description = "首页统计项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortalStatResponse {
        private String key;
        private String label;
        private String value;
        private String deltaText;
        private String trend;
        private String icon;
    }

    @Getter
    @Builder
    @Schema(name = "PortalQuickLinkResponse", description = "首页快捷链接")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortalQuickLinkResponse {
        private String key;
        private String label;
        private String description;
        private String href;
        private String type;
    }

    @Getter
    @Builder
    @Schema(name = "PortalRecentUpdateResponse", description = "首页最近更新")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortalRecentUpdateResponse {
        private String title;
        private String subtitle;
        private String summary;
        private Long updatedAt;
        private String href;
        private String coverImageUrl;
    }

    @Getter
    @Builder
    @Schema(name = "PortalFeatureCollectionResponse", description = "首页特性集合")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortalFeatureCollectionResponse {
        private String key;
        private String label;
        private String description;
        private String href;
        private String badgeText;
    }
}
