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
@Schema(name = "UploadMultipartPartRequest", description = "上传分片请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadMultipartPartRequest {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty("uploadId")
    @NotBlank(message = "uploadId不能为空")
    @Size(max = 128, message = "\"上传会话ID\"长度不能超过128")
    private String uploadId;

    @Schema(name = "partNumber", description = "分片序号，从1开始")
    @JsonProperty("partNumber")
    @NotNull(message = "partNumber不能为空")
    @Min(value = 1, message = "partNumber必须从1开始")
    private Integer partNumber;

    @Schema(name = "etag", description = "分片哈希")
    @JsonProperty("etag")
    @NotBlank(message = "etag不能为空")
    @Size(max = 128, message = "\"etag\"长度不能超过128")
    private String etag;

    @Schema(name = "size", description = "分片大小")
    @JsonProperty("size")
    @NotNull(message = "size不能为空")
    @Min(value = 1, message = "size必须大于0")
    private Long size;
}
