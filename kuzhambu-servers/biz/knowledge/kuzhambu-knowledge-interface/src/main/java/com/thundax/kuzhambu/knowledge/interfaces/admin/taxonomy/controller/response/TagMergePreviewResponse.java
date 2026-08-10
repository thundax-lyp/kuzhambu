package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "TagMergePreviewResponse", description = "标签合并影响预览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagMergePreviewResponse implements Serializable {

    @Schema(name = "sourceTag", description = "源标签")
    @JsonProperty(value = "sourceTag")
    private TagResponse sourceTag;

    @Schema(name = "targetTag", description = "目标标签")
    @JsonProperty(value = "targetTag")
    private TagResponse targetTag;

    @Schema(name = "aliasesToMerge", description = "待合并别名")
    @JsonProperty(value = "aliasesToMerge")
    private List<TagAliasResponse> aliasesToMerge;

    @Schema(name = "impactedContentRefs", description = "受影响内容引用")
    @JsonProperty(value = "impactedContentRefs")
    private List<TagContentRefResponse> impactedContentRefs;

    @Schema(name = "pendingReviewCount", description = "待审核记录数")
    @JsonProperty(value = "pendingReviewCount")
    private Integer pendingReviewCount;

    @Schema(name = "governedRecordCount", description = "治理记录数")
    @JsonProperty(value = "governedRecordCount")
    private Integer governedRecordCount;
}
