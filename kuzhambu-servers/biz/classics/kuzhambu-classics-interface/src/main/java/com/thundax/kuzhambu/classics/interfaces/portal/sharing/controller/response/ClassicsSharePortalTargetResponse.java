package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsSharePortalTargetResponse implements Serializable {
    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("contentVersionId")
    private Long contentVersionId;

    @JsonProperty("contentVersionNo")
    private Integer contentVersionNo;

    @JsonProperty("titleSnapshot")
    private String titleSnapshot;

    @JsonProperty("contentSnapshotJson")
    private String contentSnapshotJson;

    @JsonProperty("storageObject")
    private ResourceResponse storageObject;

    @JsonProperty("images")
    private List<ImageResponse> images;

    @JsonProperty("contentVisibilitySnapshot")
    private String contentVisibilitySnapshot;

    @JsonProperty("targetStatus")
    private String targetStatus;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceResponse implements Serializable {
        @JsonProperty("storageObjectId")
        private Long storageObjectId;

        @JsonProperty("originalFilename")
        private String originalFilename;

        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("size")
        private Long size;

        @JsonProperty("previewUrl")
        private String previewUrl;

        @JsonProperty("downloadUrl")
        private String downloadUrl;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResponse implements Serializable {
        @JsonProperty("imageId")
        private Long imageId;

        @JsonProperty("storageObjectId")
        private Long storageObjectId;

        @JsonProperty("originalFilename")
        private String originalFilename;

        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("size")
        private Long size;

        @JsonProperty("imageType")
        private String imageType;

        @JsonProperty("title")
        private String title;

        @JsonProperty("currentUsed")
        private Boolean currentUsed;

        @JsonProperty("storageObject")
        private ResourceResponse storageObject;
    }
}
