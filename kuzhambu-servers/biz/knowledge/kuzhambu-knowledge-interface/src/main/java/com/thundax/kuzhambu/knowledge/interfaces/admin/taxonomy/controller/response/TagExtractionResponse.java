package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagExtractionResponse", description = "AI 标签抽取响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagExtractionResponse implements Serializable {

    @Schema(name = "aiCallId", description = "AI 调用ID")
    @JsonProperty(value = "aiCallId")
    private Long aiCallId;

    @Schema(name = "aiCandidateId", description = "AI 候选ID")
    @JsonProperty(value = "aiCandidateId")
    private Long aiCandidateId;

    @Schema(name = "status", description = "AI 状态")
    @JsonProperty(value = "status")
    private String status;

    @Schema(name = "resultFormat", description = "结果格式")
    @JsonProperty(value = "resultFormat")
    private String resultFormat;

    @Schema(name = "resultPayload", description = "结构化候选 JSON")
    @JsonProperty(value = "resultPayload")
    private String resultPayload;

    @Schema(name = "errorType", description = "错误类型")
    @JsonProperty(value = "errorType")
    private String errorType;

    @Schema(name = "errorMessage", description = "错误说明")
    @JsonProperty(value = "errorMessage")
    private String errorMessage;
}
