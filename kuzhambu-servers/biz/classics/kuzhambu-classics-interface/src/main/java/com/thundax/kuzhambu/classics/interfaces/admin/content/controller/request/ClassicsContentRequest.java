package com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "古籍内容管理请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsContentRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("tagId")
    private Long tagId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("source")
    private String source;

    @JsonProperty("status")
    private String status;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("exportKind")
    private String exportKind;

    @JsonProperty("exportFormat")
    private String exportFormat;

    @JsonProperty("scopeType")
    private String scopeType;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("expiresAt")
    private Instant expiresAt;

    @JsonProperty("contentChanged")
    private Boolean contentChanged;

    @Getter
    @Setter
    @Schema(description = "AI候选结果应用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiCandidateApplyRequest {
        @NotNull(message = "candidateId不能为空")
        @JsonProperty("candidateId")
        private Long candidateId;

        @NotBlank(message = "contentType不能为空")
        @JsonProperty("contentType")
        private String contentType;

        @NotNull(message = "contentId不能为空")
        @JsonProperty("contentId")
        private Long contentId;

        @NotBlank(message = "capability不能为空")
        @JsonProperty("capability")
        private String capability;

        @JsonProperty("objectId")
        private Long objectId;

        @NotBlank(message = "resultFormat不能为空")
        @JsonProperty("resultFormat")
        private String resultFormat;

        @NotBlank(message = "resultPayload不能为空")
        @JsonProperty("resultPayload")
        private String resultPayload;

        @JsonProperty("changeSummary")
        private String changeSummary;

        @Pattern(regexp = "APPEND|COVER", message = "tagApplyMode仅支持APPEND或COVER")
        @JsonProperty("tagApplyMode")
        private String tagApplyMode;
    }

    @Getter
    @Setter
    @Schema(description = "AI候选结果批量应用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiCandidateBatchApplyRequest {
        @Valid
        @NotEmpty(message = "items不能为空")
        @JsonProperty("items")
        private List<AiCandidateApplyRequest> items;
    }

    @Getter
    @Setter
    @Schema(description = "AI候选结果批量拒绝请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiCandidateBatchRejectRequest {
        @Valid
        @NotEmpty(message = "items不能为空")
        @JsonProperty("items")
        private List<AiCandidateRejectItemRequest> items;

        @JsonProperty("errorType")
        private String errorType;

        @JsonProperty("errorMessage")
        private String errorMessage;
    }

    @Getter
    @Setter
    @Schema(description = "AI候选结果拒绝项请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiCandidateRejectItemRequest {
        @NotNull(message = "candidateId不能为空")
        @JsonProperty("candidateId")
        private Long candidateId;

        @NotBlank(message = "contentType不能为空")
        @JsonProperty("contentType")
        private String contentType;

        @NotNull(message = "contentId不能为空")
        @JsonProperty("contentId")
        private Long contentId;

        @NotBlank(message = "capability不能为空")
        @JsonProperty("capability")
        private String capability;

        @JsonProperty("objectId")
        private Long objectId;
    }
}
