package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "InitMultipartUploadRequest", description = "分片上传初始化请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitMultipartUploadRequest {

    @Schema(name = "businessType", description = "业务类型")
    @JsonProperty("businessType")
    @Size(max = 40, message = "\"业务类型\"长度不能超过40")
    private String businessType;

    @Schema(name = "ownerType", description = "引用所有者类型")
    @JsonProperty("ownerType")
    @NotBlank(message = "ownerType不能为空")
    @Size(max = 64, message = "\"引用所有者类型\"长度不能超过64")
    private String ownerType;

    @Schema(name = "ownerId", description = "引用所有者ID")
    @JsonProperty("ownerId")
    @NotBlank(message = "ownerId不能为空")
    @Size(max = 64, message = "\"引用所有者ID\"长度不能超过64")
    private String ownerId;

    @Schema(name = "originalFilename", description = "原始文件名")
    @JsonProperty("originalFilename")
    @NotBlank(message = "originalFilename不能为空")
    @Size(max = 255, message = "\"原始文件名\"长度不能超过255")
    private String originalFilename;

    @Schema(name = "mimeType", description = "内容类型")
    @JsonProperty("mimeType")
    @NotBlank(message = "mimeType不能为空")
    @Size(max = 128, message = "\"内容类型\"长度不能超过128")
    private String mimeType;

    @Schema(name = "bucketName", description = "对象存储桶")
    @JsonProperty("bucketName")
    @Size(max = 100, message = "\"对象存储桶\"长度不能超过100")
    private String bucketName;

    @Schema(name = "objectKey", description = "对象键")
    @JsonProperty("objectKey")
    @Size(max = 255, message = "\"对象键\"长度不能超过255")
    private String objectKey;

    @Schema(name = "providerUploadId", description = "外部存储上传ID")
    @JsonProperty("providerUploadId")
    @Size(max = 128, message = "\"外部存储上传ID\"长度不能超过128")
    private String providerUploadId;

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty("uploadId")
    @Size(max = 128, message = "\"上传会话ID\"长度不能超过128")
    private String uploadId;

    @Schema(name = "totalSize", description = "文件总大小")
    @JsonProperty("totalSize")
    @NotNull(message = "totalSize不能为空")
    @Min(value = 1, message = "totalSize必须大于0")
    private Long totalSize;

    @Schema(name = "partSize", description = "单分片大小")
    @JsonProperty("partSize")
    @NotNull(message = "partSize不能为空")
    @Min(value = 1, message = "partSize必须大于0")
    private Long partSize;
}
