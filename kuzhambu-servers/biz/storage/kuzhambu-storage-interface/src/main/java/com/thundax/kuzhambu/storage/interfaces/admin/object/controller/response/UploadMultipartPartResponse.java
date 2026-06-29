package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "UploadMultipartPartResponse", description = "上传分片响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadMultipartPartResponse {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty(value = "uploadId")
    private String uploadId;

    @Schema(name = "partNumber", description = "分片序号，从1开始")
    @JsonProperty(value = "partNumber")
    private Integer partNumber;

    @Schema(name = "etag", description = "分片哈希")
    @JsonProperty(value = "etag")
    private String etag;

    @Schema(name = "size", description = "分片大小")
    @JsonProperty(value = "size")
    private Long size;

    @Schema(name = "uploadStatus", description = "上传会话状态")
    @JsonProperty(value = "uploadStatus")
    private String uploadStatus;
}
