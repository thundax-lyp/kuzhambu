package com.thundax.kuzhambu.storage.interfaces.admin.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "StorageObjectResponse", description = "存储对象响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageObjectResponse implements Serializable {

    @Schema(name = "id", description = "存储对象ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "originalFilename", description = "原始文件名")
    @JsonProperty(value = "originalFilename")
    private String originalFilename;

    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    private String contentType;

    @Schema(name = "ownerId", description = "对象归属ID")
    @JsonProperty(value = "ownerId")
    private String ownerId;

    @Schema(name = "ownerType", description = "对象归属类型")
    @JsonProperty(value = "ownerType")
    private String ownerType;

    @Schema(name = "size", description = "文件大小")
    @JsonProperty(value = "size")
    private Long size;

    @Schema(name = "objectStatus", description = "对象状态")
    @JsonProperty(value = "objectStatus")
    private String objectStatus;

    @Schema(name = "referenceStatus", description = "引用状态")
    @JsonProperty(value = "referenceStatus")
    private String referenceStatus;

    @Schema(name = "priority", description = "排序值")
    @JsonProperty(value = "priority")
    private Integer priority;
}
