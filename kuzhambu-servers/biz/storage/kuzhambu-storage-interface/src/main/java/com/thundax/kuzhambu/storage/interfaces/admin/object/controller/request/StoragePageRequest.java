package com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "StoragePageRequest", description = "存储对象分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoragePageRequest extends PageRequest {

    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    @Size(max = 100, message = "\"内容类型\"长度不能超过100")
    private String contentType;

    @Schema(name = "ownerId", description = "对象归属ID")
    @JsonProperty(value = "ownerId")
    @Size(max = 64, message = "\"对象归属ID\"长度不能超过64")
    private String ownerId;

    @Schema(name = "ownerType", description = "对象归属类型")
    @JsonProperty(value = "ownerType")
    @Size(max = 40, message = "\"对象归属类型\"长度不能超过40")
    private String ownerType;

    @Schema(name = "objectStatus", description = "对象状态")
    @JsonProperty(value = "objectStatus")
    @Size(max = 40, message = "\"对象状态\"长度不能超过40")
    private String objectStatus;

    @Schema(name = "referenceStatus", description = "引用状态")
    @JsonProperty(value = "referenceStatus")
    @Size(max = 40, message = "\"引用状态\"长度不能超过40")
    private String referenceStatus;

    @Schema(name = "referenceOwnerId", description = "引用归属ID")
    @JsonProperty(value = "referenceOwnerId")
    @Size(max = 64, message = "\"引用归属ID\"长度不能超过64")
    private String referenceOwnerId;

    @Schema(name = "referenceOwnerType", description = "引用归属类型")
    @JsonProperty(value = "referenceOwnerType")
    @Size(max = 40, message = "\"引用归属类型\"长度不能超过40")
    private String referenceOwnerType;

    @Schema(name = "originalFilename", description = "原始文件名，模糊查询")
    @JsonProperty(value = "originalFilename")
    @Size(max = 255, message = "\"原始文件名\"长度不能超过255")
    private String originalFilename;

    @Schema(name = "remarks", description = "备注，模糊查询")
    @JsonProperty(value = "remarks")
    @Size(max = 255, message = "\"备注\"长度不能超过255")
    private String remarks;

    @Schema(name = "sortDirection", description = "排序方向")
    @JsonProperty(value = "sortDirection")
    @Size(max = 10, message = "\"排序方向\"长度不能超过10")
    private String sortDirection;
}
