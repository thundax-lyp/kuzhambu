package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/** 图谱素材删除流程 HTTP 响应契约。 */
public final class GraphDeletionResponses {
    private GraphDeletionResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ChangeData(
            String id,
            GraphMaterialResponses.ContentRefData contentRef,
            String status,
            String decision,
            String lockVersion,
            Map<String, Object> sourceSnapshot,
            String requestedAt,
            String decidedAt) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskData(
            String id,
            String status,
            String progress,
            String inputSnapshotVersion,
            String resultSummary,
            String failureReason,
            String retryFromTaskId,
            String requestedAt,
            String completedAt) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ChangePageData(
            String pageNo, String pageSize, String totalCount, String totalPage, List<ChangeData> records) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskPageData(
            String pageNo, String pageSize, String totalCount, String totalPage, List<TaskData> records) {}
}
