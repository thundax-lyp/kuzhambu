package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "古籍发布批量操作结果项")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationBatchItemResponse {
    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "是否受理")
    private boolean accepted;

    @Schema(description = "发布任务ID")
    private Long jobId;

    @Schema(description = "失败原因")
    private String reason;

    public ClassicsPublicationBatchItemResponse(Long contentId, boolean accepted, Long jobId, String reason) {
        this.contentId = contentId;
        this.accepted = accepted;
        this.jobId = jobId;
        this.reason = reason;
    }

    public Long contentId() {
        return contentId;
    }

    public boolean accepted() {
        return accepted;
    }

    public Long jobId() {
        return jobId;
    }

    public String reason() {
        return reason;
    }
}
