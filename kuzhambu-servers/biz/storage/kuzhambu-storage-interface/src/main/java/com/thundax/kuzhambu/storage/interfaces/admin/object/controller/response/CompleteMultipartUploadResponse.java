package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "CompleteMultipartUploadResponse", description = "完成分片上传响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteMultipartUploadResponse {

    @Schema(name = "id", description = "存储对象ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty(value = "uploadId")
    private String uploadId;

    @Schema(name = "businessType", description = "业务类型")
    @JsonProperty(value = "businessType")
    private String businessType;

    @Schema(name = "originalFilename", description = "原始文件名")
    @JsonProperty(value = "originalFilename")
    private String originalFilename;

    @Schema(name = "mimeType", description = "内容类型")
    @JsonProperty(value = "mimeType")
    private String mimeType;

    @Schema(name = "bucketName", description = "对象存储桶")
    @JsonProperty(value = "bucketName")
    private String bucketName;

    @Schema(name = "objectKey", description = "对象键")
    @JsonProperty(value = "objectKey")
    private String objectKey;

    @Schema(name = "size", description = "对象大小")
    @JsonProperty(value = "size")
    private Long size;

    @Schema(name = "accessEndpoint", description = "内容读取入口")
    @JsonProperty(value = "accessEndpoint")
    private String accessEndpoint;

    @Schema(name = "objectStatus", description = "对象状态")
    @JsonProperty(value = "objectStatus")
    private String objectStatus;

    @Schema(name = "referenceStatus", description = "引用状态")
    @JsonProperty(value = "referenceStatus")
    private String referenceStatus;

    @Schema(name = "providerUploadId", description = "外部存储上传ID")
    @JsonProperty(value = "providerUploadId")
    private String providerUploadId;
}
