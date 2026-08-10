package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "古籍发布批量操作响应")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationBatchResponse {
    @Schema(description = "受理数量")
    private long acceptedCount;

    @Schema(description = "拒绝数量")
    private long rejectedCount;

    @Schema(description = "结果项列表")
    private List<ClassicsPublicationBatchItemResponse> items;

    public ClassicsPublicationBatchResponse(
            long acceptedCount, long rejectedCount, List<ClassicsPublicationBatchItemResponse> items) {
        this.acceptedCount = acceptedCount;
        this.rejectedCount = rejectedCount;
        this.items = items;
    }

    public long acceptedCount() {
        return acceptedCount;
    }

    public long rejectedCount() {
        return rejectedCount;
    }

    public List<ClassicsPublicationBatchItemResponse> items() {
        return items;
    }
}
