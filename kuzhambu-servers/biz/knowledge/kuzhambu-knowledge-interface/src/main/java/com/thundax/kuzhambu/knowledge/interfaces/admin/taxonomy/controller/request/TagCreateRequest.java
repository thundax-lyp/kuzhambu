package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagCreateRequest", description = "标签创建请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCreateRequest {

    @Schema(name = "id", description = "标签ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "name", description = "标签名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"标签名称\"不能为空")
    @Size(max = 128, message = "\"标签名称\"长度不能超过128")
    private String name;

    @Schema(name = "categoryId", description = "分类ID")
    @JsonProperty(value = "categoryId")
    @Size(max = 64, message = "\"分类ID\"长度不能超过64")
    private String categoryId;

    @Schema(name = "description", description = "标签描述")
    @JsonProperty(value = "description")
    @Size(max = 1024, message = "\"标签描述\"长度不能超过1024")
    private String description;

    @Schema(name = "reviewStatus", description = "审核状态")
    @JsonProperty(value = "reviewStatus")
    @Size(max = 32, message = "\"审核状态\"长度不能超过32")
    private String reviewStatus;

    @Schema(name = "reviewNote", description = "审核说明")
    @JsonProperty(value = "reviewNote")
    @Size(max = 512, message = "\"审核说明\"长度不能超过512")
    private String reviewNote;

    @Schema(name = "reviewedAt", description = "审核时间")
    @JsonProperty(value = "reviewedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewedAt;
}
