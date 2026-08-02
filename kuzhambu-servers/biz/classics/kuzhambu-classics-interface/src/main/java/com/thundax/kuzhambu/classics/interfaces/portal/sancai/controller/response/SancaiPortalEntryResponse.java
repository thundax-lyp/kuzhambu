package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "三才图会门户条目响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiPortalEntryResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("volumeId")
    private Long volumeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("originalText")
    private String originalText;

    @JsonProperty("translationText")
    private String translationText;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("lifecycleStatus")
    private String lifecycleStatus;

    @JsonProperty("translationStatus")
    private String translationStatus;

    @JsonProperty("imageStatus")
    private String imageStatus;

    @JsonProperty("visualAssetStatus")
    private String visualAssetStatus;

    @JsonProperty("refinementStatus")
    private String refinementStatus;

    @JsonProperty("contentUpdatedAt")
    private Instant contentUpdatedAt;

    @JsonProperty("tags")
    private List<TagResponse> tags;

    @JsonProperty("images")
    private List<ImageResponse> images;

    @JsonProperty("currentVisualAsset")
    private VisualAssetResponse currentVisualAsset;

    @Getter
    @Builder
    @Schema(description = "三才图会门户条目标签响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TagResponse implements Serializable {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("tagId")
        private Long tagId;

        @JsonProperty("tagName")
        private String tagName;

        @JsonProperty("source")
        private String source;
    }

    @Getter
    @Builder
    @Schema(description = "三才图会门户条目图片响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResponse implements Serializable {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("imageType")
        private String imageType;

        @JsonProperty("currentUsed")
        private Boolean currentUsed;

        @JsonProperty("previewUrl")
        private String previewUrl;

        @JsonProperty("downloadUrl")
        private String downloadUrl;
    }

    @Getter
    @Builder
    @Schema(description = "三才图会门户当前视觉资产响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VisualAssetResponse implements Serializable {
        @JsonProperty("visualAssetId")
        private Long visualAssetId;

        @JsonProperty("versionNo")
        private Integer versionNo;

        @JsonProperty("status")
        private String status;

        @JsonProperty("imageAnalysisMarkdown")
        private String imageAnalysisMarkdown;

        @JsonProperty("fusionDescription")
        private String fusionDescription;

        @JsonProperty("visualDescription")
        private String visualDescription;

        @JsonProperty("sourcePreviewUrl")
        private String sourcePreviewUrl;

        @JsonProperty("generatedPreviewUrl")
        private String generatedPreviewUrl;
    }
}
