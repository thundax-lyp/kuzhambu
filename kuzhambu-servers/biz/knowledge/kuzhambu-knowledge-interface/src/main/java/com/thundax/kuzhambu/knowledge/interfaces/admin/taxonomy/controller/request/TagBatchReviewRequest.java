package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagBatchReviewRequest", description = "标签批量审核请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagBatchReviewRequest {

    @Schema(name = "tagIds", description = "标签ID列表")
    @JsonProperty(value = "tagIds")
    @NotEmpty(message = "\"标签ID列表\"不能为空")
    private List<@Size(max = 64, message = "\"标签ID\"长度不能超过64") String> tagIds;

    @Schema(name = "decision", description = "审核决策，APPROVE/REJECT")
    @JsonProperty(value = "decision")
    @NotEmpty(message = "\"审核决策\"不能为空")
    @Size(max = 16, message = "\"审核决策\"长度不能超过16")
    private String decision;

    @Schema(name = "categoryId", description = "正式分类ID")
    @JsonProperty(value = "categoryId")
    @Size(max = 64, message = "\"正式分类ID\"长度不能超过64")
    private String categoryId;

    @Schema(name = "reviewNote", description = "审核说明")
    @JsonProperty(value = "reviewNote")
    @Size(max = 512, message = "\"审核说明\"长度不能超过512")
    private String reviewNote;
}
