package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagCandidateApplyRequest", description = "AI 标签候选应用请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCandidateApplyRequest {

    @Schema(name = "aiCandidateId", description = "AI 候选ID")
    @JsonProperty(value = "aiCandidateId")
    @NotNull(message = "\"AI 候选ID\"不能为空")
    private Long aiCandidateId;

    @Schema(name = "selectedTags", description = "选中的候选标签")
    @JsonProperty(value = "selectedTags")
    @Valid
    @NotEmpty(message = "\"选中的候选标签\"不能为空")
    private List<TagCandidateApplyItemRequest> selectedTags;

    @Schema(name = "reviewNote", description = "审核备注")
    @JsonProperty(value = "reviewNote")
    private String reviewNote;

    @Schema(name = "reviewedBy", description = "审核人")
    @JsonProperty(value = "reviewedBy")
    @NotNull(message = "\"审核人\"不能为空")
    private Long reviewedBy;

    @Getter
    @Setter
    @Schema(name = "TagCandidateApplyItemRequest", description = "AI 标签候选项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TagCandidateApplyItemRequest {

        @Schema(name = "name", description = "候选标签名")
        @JsonProperty(value = "name")
        @NotBlank(message = "\"候选标签名\"不能为空")
        private String name;

        @Schema(name = "categoryId", description = "分类ID")
        @JsonProperty(value = "categoryId")
        private String categoryId;

        @Schema(name = "categoryName", description = "分类名")
        @JsonProperty(value = "categoryName")
        private String categoryName;

        @Schema(name = "confidence", description = "置信度")
        @JsonProperty(value = "confidence")
        private BigDecimal confidence;

        @Schema(name = "reason", description = "抽取理由")
        @JsonProperty(value = "reason")
        private String reason;

        @Schema(name = "matchedExistingTagId", description = "匹配既有标签ID")
        @JsonProperty(value = "matchedExistingTagId")
        private String matchedExistingTagId;
    }
}
