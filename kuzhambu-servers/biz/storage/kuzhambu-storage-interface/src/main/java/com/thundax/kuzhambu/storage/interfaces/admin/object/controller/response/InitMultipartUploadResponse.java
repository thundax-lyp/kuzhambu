package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "InitMultipartUploadResponse", description = "分片上传初始化响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitMultipartUploadResponse {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty("uploadId")
    private String uploadId;

    @Schema(name = "providerUploadId", description = "外部存储上传ID")
    @JsonProperty("providerUploadId")
    private String providerUploadId;

    @Schema(name = "businessType", description = "业务类型")
    @JsonProperty("businessType")
    private String businessType;

    @Schema(name = "originalFilename", description = "原始文件名")
    @JsonProperty("originalFilename")
    private String originalFilename;

    @Schema(name = "mimeType", description = "内容类型")
    @JsonProperty("mimeType")
    private String mimeType;

    @Schema(name = "bucketName", description = "对象存储桶")
    @JsonProperty("bucketName")
    private String bucketName;

    @Schema(name = "objectKey", description = "对象键")
    @JsonProperty("objectKey")
    private String objectKey;

    @Schema(name = "totalSize", description = "文件总大小")
    @JsonProperty("totalSize")
    private Long totalSize;

    @Schema(name = "partSize", description = "分片大小")
    @JsonProperty("partSize")
    private Long partSize;

    @Schema(name = "uploadedPartCount", description = "已上传分片数量")
    @JsonProperty("uploadedPartCount")
    private Integer uploadedPartCount;

    @Schema(name = "uploadStatus", description = "上传会话状态")
    @JsonProperty("uploadStatus")
    private String uploadStatus;
}
