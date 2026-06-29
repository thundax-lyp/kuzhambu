package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "CompleteMultipartUploadRequest", description = "完成分片上传请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteMultipartUploadRequest {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty("uploadId")
    @NotBlank(message = "uploadId不能为空")
    @Size(max = 128, message = "\"上传会话ID\"长度不能超过128")
    private String uploadId;

    @Schema(name = "bucketName", description = "对象存储桶")
    @JsonProperty("bucketName")
    @Size(max = 100, message = "\"对象存储桶\"长度不能超过100")
    private String bucketName;

    @Schema(name = "objectKey", description = "对象键")
    @JsonProperty("objectKey")
    @Size(max = 255, message = "\"对象键\"长度不能超过255")
    private String objectKey;

    @Schema(name = "size", description = "对象大小覆盖值")
    @JsonProperty("size")
    private Long size;

    @Schema(name = "accessEndpoint", description = "对象读取入口")
    @JsonProperty("accessEndpoint")
    @Size(max = 255, message = "\"对象读取入口\"长度不能超过255")
    private String accessEndpoint;
}
