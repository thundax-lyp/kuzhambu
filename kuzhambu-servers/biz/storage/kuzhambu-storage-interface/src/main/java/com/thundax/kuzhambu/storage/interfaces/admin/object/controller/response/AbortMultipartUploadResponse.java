package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AbortMultipartUploadResponse", description = "中止分片上传响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbortMultipartUploadResponse {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty(value = "uploadId")
    private String uploadId;

    @Schema(name = "uploadStatus", description = "上传会话状态")
    @JsonProperty(value = "uploadStatus")
    private String uploadStatus;
}
