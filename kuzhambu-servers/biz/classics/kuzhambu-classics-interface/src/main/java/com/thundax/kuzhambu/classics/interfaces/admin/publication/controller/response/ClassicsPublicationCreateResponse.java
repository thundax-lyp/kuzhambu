package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "古籍发布任务创建响应")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationCreateResponse {
    @Schema(description = "发布任务ID")
    private Long jobId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "当前生命周期状态")
    private String lifecycleStatus;

    @Schema(description = "当前发布流转状态")
    private String transitionStatus;

    public ClassicsPublicationCreateResponse(
            Long jobId, String contentType, Long contentId, String lifecycleStatus, String transitionStatus) {
        this.jobId = jobId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.lifecycleStatus = lifecycleStatus;
        this.transitionStatus = transitionStatus;
    }

    public Long jobId() {
        return jobId;
    }

    public String contentType() {
        return contentType;
    }

    public Long contentId() {
        return contentId;
    }

    public String lifecycleStatus() {
        return lifecycleStatus;
    }

    public String transitionStatus() {
        return transitionStatus;
    }
}
