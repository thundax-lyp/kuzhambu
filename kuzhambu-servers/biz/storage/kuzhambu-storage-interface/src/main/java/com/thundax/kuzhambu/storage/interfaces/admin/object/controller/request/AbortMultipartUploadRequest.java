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
@Schema(name = "AbortMultipartUploadRequest", description = "中止分片上传请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbortMultipartUploadRequest {

    @Schema(name = "uploadId", description = "上传会话ID")
    @JsonProperty("uploadId")
    @NotBlank(message = "uploadId不能为空")
    @Size(max = 128, message = "\"上传会话ID\"长度不能超过128")
    private String uploadId;
}
